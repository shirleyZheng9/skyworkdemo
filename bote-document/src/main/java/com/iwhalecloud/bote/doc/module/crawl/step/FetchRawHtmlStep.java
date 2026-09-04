package com.iwhalecloud.bote.doc.module.crawl.step;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.doc.common.utils.AntiBotUtil;
import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

/**
 * 步骤执行器：获取原始 HTML
 *
 * <p>采用 Playwright 服务</p>
 *
 * <p>支持的 PlayWright 配置:</p>
 *
 * <ul>
 *   <li>PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD:
 *           原生支持的环境变量，设置为 1 可禁止初始化时自动下载浏览器。
 *           代码位置: {@link com.microsoft.playwright.impl.driver.jar.DriverJar#installBrowsers}</li>
 *   <li>PLAYWRIGHT_NODEJS_PATH:
 *           原生支持的环境变量，用于指定 node.js 命令的路径，默认使用 driver-bundle 内置的 node。
 *           代码位置: {@link com.microsoft.playwright.impl.driver.Driver#createProcessBuilder}</li>
 *   <li>playwright.cli.dir: 原生支持的系统参数，用于指定驱动包路径，默认从 driver-bundle 解压缩，指定这个路径可以避免依赖 driver-bundle。
 *           注意: 只支持系统参数，不支持 Spring Boot 的配置机制。
 *           代码位置: {@link com.microsoft.playwright.impl.driver.Driver#newInstance}</li>
 *   <li>playwright.chromium.executable.path:
 *           自定义配置项，用于指定 Chromium 可执行文件的绝对路径。
 *           如果不指定，PlayWright 会根据自身版本、缓存目录自动构造一个路径（但文件不一定存在，除非开启了自动下载）。
 *           指定这个路径可以使用通过其它方式安装的浏览器，比如 Linux 中使用系统包管理器安装的 Chromium</li>
 * </ul>
 *
 * <p>不同部署方式需要注意:</p>
 *
 * <ul>
 *   <li>容器部署: 基础镜像已预装 chromium-headless 并设置了相关环境变量，不需要特殊处理</li>
 *   <li>非容器部署: 配置 PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 以避免自动下载浏览器，然后手动安装 Chromium headless, 并设置 playwright.chromium.executable.path;
 *                 可以使用 playwright 安装（理论上兼容性更好），也可以使用系统包管理器安装。
 *   </li>
 *   <li>本地开发: 无需特殊处理，让 PlayWright 自动下载浏览器</li>
 * </ul>
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class FetchRawHtmlStep extends AbstractCrawlStep {
  /** Chromium 可执行文件路径 */
  private static Path chromiumExecutablePath;
  /** 30 秒超时 */
  private static final int PLAYWRIGHT_TIMEOUT = 30000;
  /** 页面加载后等待1秒，确保 JavaScript 执行完成（基础策略） */
  private static final int PLAYWRIGHT_WAIT_AFTER_LOAD_BASE = 1000;
  /** 模拟桌面浏览器 UA */
  private static final String DESKTOP_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
  /** 模拟移动端浏览器 UA（反爬升级策略使用） */
  private static final String MOBILE_USER_AGENT =
    "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

  static {
    initPlaywright();
  }

  /**
   * 抓取策略枚举。
   *
   * <p>按照顺序依次尝试：
   * <ul>
   *   <li>NORMAL: 桌面 UA + 基础等待</li>
   *   <li>MOBILE: 移动 UA + 更长等待，模拟“换设备/指纹”</li>
   *   <li>HTTP_FALLBACK: 简单 HTTP 抓取（无 JS），作为兜底策略</li>
   * </ul>
   * </p>
   */
  private enum Strategy {
    NORMAL,
    MOBILE,
    HTTP_FALLBACK
  }

  public FetchRawHtmlStep(String url, CrawlResult result) {
    super(url, result);
  }

  /**
   * 初始化 Playwright 配置
   */
  private static void initPlaywright() {
    // Chromium 可执行文件路径
    String executablePath = SpringUtil.getProperty("playwright.chromium.executable.path");
    if (StringUtils.isNotEmpty(executablePath)) {
      Path path = Paths.get(executablePath);
      Assert.isTrue(path.toFile().exists(), () -> "playwright.chromium.executable.path 指定的路径不存在: " + executablePath);
      chromiumExecutablePath = path;
    }
  }

  @Override
  protected ResultVO<String> doExecute() {
    // 按策略逐级升级，尽量接近 Firecrawl 的“engine waterfall” 逻辑
    Strategy[] strategies = {Strategy.NORMAL, Strategy.MOBILE, Strategy.HTTP_FALLBACK};

    // 记录最后一次成功获取的内容（即使是反爬页面），以便在所有策略都失败时返回
    String lastContent = null;
    boolean lastContentIsAntiBot = false;

    for (Strategy strategy : strategies) {
      try {
        String content = fetchContent(strategy);
        if (StringUtils.isBlank(content)) {
          logger.warn("抓取结果为空，策略={} url={}", strategy, url);
          continue;
        }

        // 记录最后一次成功获取的内容
        lastContent = content;
        lastContentIsAntiBot = AntiBotUtil.isLikelyAntiBotPage(content);

        // 反爬虫拦截检测：如果判断为疑似人机验证 / 反爬页面，则升级到下一策略
        if (lastContentIsAntiBot) {
          logger.warn("检测到疑似反爬 / 人机验证页面，准备升级策略: strategy={} url={}", strategy, url);
          continue;
        }

        // 成功拿到正常页面内容
        result.setHtmlContent(content);
        return ResultVO.success();
      }
      catch (Exception e) {
        // 网络问题不需要重试，直接返回失败
        if (Strings.CI.containsAny(e.getMessage(), "网络", "超时", "timeout")) {
          return ResultVO.fail(e.getMessage());
        }
        logger.warn("使用策略抓取页面失败: strategy={} url={}, error={}", strategy, url, e.getMessage(), e);
        // 失败则尝试下一策略
      }
    }

    // 所有策略都失败或被反爬拦截
    // 如果最后一次成功获取到了内容（即使是反爬页面），也返回它，而不是返回通用错误
    // 这样上层代码至少能看到原始响应，而不是完全丢失信息
    if (lastContent != null) {
      logger.warn("所有策略都失败或被反爬拦截，返回最后一次获取的内容（可能是反爬页面）: url={}, isAntiBot={}",
        url, lastContentIsAntiBot);
      result.setHtmlContent(lastContent);
      if (lastContentIsAntiBot) {
        // 如果是反爬页面，返回失败但保留内容，让上层能看到原始响应
        String msg = AntiBotUtil.buildAntiBotHintMessage(url);
        return ResultVO.fail(msg);
      }
      // 如果不是反爬页面但所有策略都失败，也返回内容
      return ResultVO.success();
    }

    // 所有策略都完全失败，没有获取到任何内容
    String msg = AntiBotUtil.buildAntiBotHintMessage(url);
    return ResultVO.fail(msg);
  }

  /**
   * 抓取网页内容
   */
  private String fetchContent(Strategy strategy) {
    String content;
    switch (strategy) {
      case NORMAL -> {
        logger.debug("使用 NORMAL 策略抓取页面: url={}", url);
        content = fetchWithPlaywright(DESKTOP_USER_AGENT, PLAYWRIGHT_WAIT_AFTER_LOAD_BASE);
      }
      case MOBILE -> {
        logger.debug("使用 MOBILE 策略抓取页面（移动 UA + 更长等待）: url={}", url);
        content = fetchWithPlaywright(MOBILE_USER_AGENT, PLAYWRIGHT_WAIT_AFTER_LOAD_BASE * 3);
      }
      case HTTP_FALLBACK -> {
        logger.debug("使用 HTTP_FALLBACK 策略（简单 HTTP 抓取）: url={}", url);
        content = fetchWithSimpleHttp();
      }
      default -> throw new IllegalStateException("未知抓取策略: " + strategy);
    }
    return content;
  }

  /**
   * 使用 Playwright 抓取页面内容（单次策略执行）
   */
  private String fetchWithPlaywright(String userAgent, int waitAfterLoadMillis) {
    Playwright playwright = null; //NOPMD - suppressed CloseResource - 已使用 finally 关闭
    Browser browser = null; //NOPMD - suppressed CloseResource - 已使用 finally 关闭
    BrowserContext context = null; //NOPMD - suppressed CloseResource - 已使用 finally 关闭
    Page page = null; //NOPMD - suppressed CloseResource - 已使用 finally 关闭
    try {
      playwright = Playwright.create();

      // 使用 Chromium 浏览器，headless 模式
      BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
      launchOptions.setHeadless(true);
      launchOptions.setExecutablePath(chromiumExecutablePath);
      browser = playwright.chromium().launch(launchOptions);

      // 创建浏览器上下文
      Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
      contextOptions.setUserAgent(userAgent);
      // 忽略 HTTPS 证书错误
      contextOptions.setIgnoreHTTPSErrors(true);
      contextOptions.setLocale("zh-CN");
      context = browser.newContext(contextOptions);

      // 创建新页面
      page = context.newPage();

      // 导航到目标URL
      Page.NavigateOptions navigateOptions = new Page.NavigateOptions();
      navigateOptions.setWaitUntil(WaitUntilState.DOMCONTENTLOADED);
      navigateOptions.setTimeout(PLAYWRIGHT_TIMEOUT);
      page.navigate(url, navigateOptions);

      // 等待页面网络空闲（确保动态内容加载完成）
      try {
        page.waitForLoadState(LoadState.NETWORKIDLE,
          new Page.WaitForLoadStateOptions().setTimeout(PLAYWRIGHT_TIMEOUT));
      }
      catch (Exception e) {
        // 如果网络空闲等待超时，继续尝试获取内容
        logger.debug("等待网络空闲超时，继续获取内容: url={}", url);
      }

      // 等待页面加载完成（确保 JavaScript 执行）
      if (waitAfterLoadMillis > 0) {
        page.waitForTimeout(waitAfterLoadMillis);
      }

      // 等待页面稳定（确保没有正在进行的导航）
      waitForPageStable(page);

      // 获取页面内容（使用重试机制）
      return getPageContentWithRetry(page);
    }
    catch (TimeoutError e) {
      throw new BssException("打开网页超时，请检查网络", e);
    }
    catch (Exception e) {
      if (Strings.CI.containsAny("ERR_CONNECTION_RESET", "ERR_NAME_NOT_RESOLVED")) {
        throw new BssException("网络不通，请检查网络");
      }
      throw new RuntimeException("使用 Playwright 抓取页面失败: " + ExpUtil.getMsg(e), e);
    }
    finally {
      clear(page, context, browser, playwright);
    }
  }

  /**
   * 使用简单 HTTP 客户端抓取页面内容（无 JS 渲染），作为兜底策略。
   */
  private String fetchWithSimpleHttp() {
    // 自定义超时时间，默认的超时时间太长了
    OkHttpClient okHttpClient = ModelHttpClient.getClient().newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .readTimeout(Duration.ofSeconds(30))
      .callTimeout(Duration.ofSeconds(30))
      .retryOnConnectionFailure(false)
      .build();
    Request request = new Builder().url(url).header(HttpHeaders.USER_AGENT, DESKTOP_USER_AGENT).build();
    Call call = okHttpClient.newCall(request);
    try (Response response = call.execute()) {
      if (response.code() != 200) {
        throw new BssException("HTTP 抓取失败: status=" + response.code());
      }
      String content = ModelHttpClient.readResponseBody(response);
      if (StringUtils.isEmpty(content)) {
        throw new BssException("HTTP 抓取结果为空");
      }
      return content;
    }
    catch (BssException e) {
      throw e;
    }
    catch (SocketTimeoutException e) {
      throw new BssException("请求超时，请检查网络", e);
    }
    catch (Exception e) {
      if (Strings.CI.containsAny(e.getMessage(), "connection reset", "Name or service not known")) {
        throw new BssException("网络不通，请检查网络", e);
      }
      throw new BssException("HTTP 抓取失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 等待页面稳定（确保没有正在进行的导航）
   *
   * @param page 页面对象
   */
  private void waitForPageStable(Page page) {
    try {
      // 等待一小段时间，确保没有正在进行的导航
      page.waitForTimeout(500);

      // 检查页面是否处于稳定状态
      // 如果页面正在导航，waitForLoadState 会抛出异常
      page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout(2000));
    }
    catch (Exception e) {
      // 如果页面仍在导航，等待更长时间
      logger.debug("页面可能仍在导航，等待更长时间: {}", e.getMessage());
      try {
        page.waitForTimeout(2000);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout(5000));
      }
      catch (Exception e2) {
        logger.warn("等待页面稳定超时，继续尝试获取内容: {}", e2.getMessage());
      }
    }
  }

  /**
   * 使用重试机制获取页面内容
   *
   * @param page 页面对象
   * @return 页面 HTML 内容
   */
  private String getPageContentWithRetry(Page page) {
    int maxRetries = 3;
    int retryDelay = 1000; // 1秒

    for (int i = 0; i < maxRetries; i++) {
      try {
        // 再次确保页面稳定
        page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout(2000));
        return page.content();
      }
      catch (Exception e) {
        if (i < maxRetries - 1) {
          logger.debug("获取页面内容失败，准备第 {} 次重试: {}", i + 1, e.getMessage());
          try {
            // 等待一段时间后重试
            page.waitForTimeout(retryDelay);
            // 增加重试延迟
            retryDelay *= 2;
          }
          catch (Exception e2) {
            logger.warn("等待重试时发生异常: {}", e2.getMessage());
          }
        }
        else {
          // 最后一次重试失败，抛出异常
          logger.error("获取页面内容失败，已重试 {} 次: {}", maxRetries, e.getMessage());
          throw e;
        }
      }
    }

    // 理论上不会到达这里
    return page.content();
  }

  private void clear(Page page, BrowserContext context, Browser browser, Playwright playwright) {
    // 清理资源
    try {
      if (page != null) {
        page.close();
      }
      if (context != null) {
        context.close();
      }
      if (browser != null) {
        browser.close();
      }
      if (playwright != null) {
        playwright.close();
      }
    }
    catch (Exception e) {
      logger.warn("清理 Playwright 资源时发生异常: {}", e.getMessage());
    }
  }
}
