package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 网站内容获取、url连接提取工具类
 * 1.基于JSoup实现，用于获取网站内容
 * 2.基于正则表达式提前文本里面的url
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class WebPageFetchUtil {

  private static final Logger logger = LoggerFactory.getLogger(WebPageFetchUtil.class);

  // 网页访问超时时间，10秒
  private static final int DEFAULT_TIMEOUT = 10000;
  // 响应内容最大允许值，20MB
  private static final int DEFAULT_MAX_BODY_SIZE = 20 * 1024 * 1024;
  // 失败重试次数
  private static final int DEFAULT_RETRY_COUNT = 2;
  // 模拟浏览器设置
  private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
  // 需要删除的标签
  private static final String DEFAULT_REMOVE_ELEMENTS = "script, style, iframe, nav, footer, .advertisement, .ads, .ad";
  // 安全校验配置，URL不能包含如下关键字
  private static final List<String> FORBIDDEN_PROTOCOLS = Arrays.asList(
    "file", "gopher", "ftp", "ftps", "jar", "mailto", "ssh2", "telent", "expect"
  );
  // 安全校验配置，禁止访问本地IP
  private static final Pattern LOCAL_IP_PATTERN = Pattern.compile(
    "^(127\\.0\\.0\\.1|192\\.168\\..*|::1|0:0:0:0:0:0:0:1)$", Pattern.CASE_INSENSITIVE
  );
  // 安全校验配置，禁止访问localhost
  private static final String LOCALHOST = "localhost";


  // URL提取：URL正则表达式模式，支持各种格式的HTTP/HTTPS链接
  private static final Pattern URL_PATTERN = Pattern.compile(
    "https?://" +  // 协议
      "(?:[\\w-]{1,63}\\.){1,5}[\\w-]{1,63}" +  // 域名（限制子域名数量和长度避免ReDOS）
      "(?::[\\d]{1,5})?" +  // 端口号
      "(?:/[\\x21-\\x7E]{0,200})?" +  // 路径和参数（只匹配非空格ASCII字符，遇到空格或中文停止）
      "(?:#[\\x21-\\x7E]{0,200})?"  // 锚点（只匹配非空格ASCII字符，遇到空格或中文停止）
  );

  // URL提取：专门处理localhost的正则表达式模式
  private static final Pattern LOCALHOST_URL_PATTERN = Pattern.compile(
    "https?://" +  // 协议
      "localhost" +  // localhost域名
      "(?::[\\d]{1,5})?" +  // 端口号
      "(?:/[\\x21-\\x7E]{0,200})?" +  // 路径和参数（只匹配非空格ASCII字符，遇到空格或中文停止）
      "(?:#[\\x21-\\x7E]{0,200})?"  // 锚点（只匹配非空格ASCII字符，遇到空格或中文停止）
  );
  // URL检查域名部分是否包含有效的字符
  private static final String URL_VALID_PATTERN = "^[\\w.-]+(:[\\d]{1,5})?(/.*)?$";
  // URL检查域名部分是否包含有效的字符 localhost
  private static final String URL_VALID_LOCALHOST_PATTERN = "^localhost(:[\\d]{1,5})?(/.*)?$";

  @SuppressWarnings("HttpUrlsUsage")
  private static final String URL_PREFIX_HTTP = "http://";
  private static final String URL_PREFIX_HTTPS = "https://";
  private static final String URL_VALID_STR = "://";
  private static final String URL_LOCALHOST = "localhost";


  // 允许的URL字符集，包括字母、数字、标点符号和特殊字符。
  private static final String VALID_URL_CHARS = "-._~:/?#[]@!$&'()*+,;=%";
  private static final String INVALID_END_CHARS = ",。，、；：！？";

  private WebPageFetchUtil() {

  }

  /**
   * 获取网站内容
   *
   * @param url 网站URL
   * @return 网站内容字符串
   */
  public static String fetchContent(String url) {
    if (StringUtils.isEmpty(url)) {
      throw new BssException("URL不能为空");
    }

    // 验证URL安全性
    validateUrl(url);

    url = url.trim();
    logger.info("开始获取网站内容: {}", url);
    int retryCount = 0;
    while (true) {
      try {
        // 使用JSoup获取HTML内容
        Connection.Response resp = Jsoup.connect(url)
          .timeout(DEFAULT_TIMEOUT)
          .maxBodySize(DEFAULT_MAX_BODY_SIZE)
          .userAgent(DEFAULT_USER_AGENT)
          .followRedirects(false) // 禁用重定向
          .ignoreContentType(true)
          .method(Connection.Method.GET) // 只支持GET请求
          .execute();
        // 只处理200返回的值
        if (resp.statusCode() != 200) {
          throw new BssException("请求失败，状态码: " + resp.statusCode());
        }
        Document doc = resp.parse();
        // 移除不需要的元素
        doc.select(DEFAULT_REMOVE_ELEMENTS).remove();
        // 获取文本内容
        String content = doc.body().text();
        if (StringUtils.isBlank(content)) {
          logger.warn("获取到的内容为空: {}", url);
          return "";
        }
        logger.info("网站内容获取完成: {}, 内容长度: {}", url, content.length());
        return content;

      } catch (ConnectException e) {
        throw new BssException("连接失败，请检查地址是否能访问: " + url, e);
      } catch (IOException e) {
        retryCount++;
        if (retryCount >= DEFAULT_RETRY_COUNT) {
          logger.error("获取网站内容失败，重试次数已达上限: {}", url, e);
          throw new BssException("获取网站内容失败: " + e.getMessage(), e);
        }
        logger.warn("获取网站内容失败，准备第{}次重试: {}", retryCount, url);
      }
    }
  }

  /**
   * 验证URL安全性，防止SSRF漏洞
   *
   * @param url 待验证的URL
   */
  private static void validateUrl(String url) {
    try {
      // 解析URL
      URI uri = new URI(url);
      String scheme = uri.getScheme();
      String host = uri.getHost();

      // 检查协议
      if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
        throw new BssException("只支持http和https协议");
      }

      // 检查是否包含禁用协议关键字
      for (String forbiddenProtocol : FORBIDDEN_PROTOCOLS) {
        if (url.toLowerCase().contains(forbiddenProtocol + "://")) {
          throw new BssException("URL包含禁用协议: " + forbiddenProtocol);
        }
      }

      // 检查是否包含多个http或https
      //noinspection HttpUrlsUsage
      int httpCount = StringUtils.countMatches(url.toLowerCase(), "http://");
      int httpsCount = StringUtils.countMatches(url.toLowerCase(), "https://");
      if (httpCount > 1 || httpsCount > 1) {
        throw new BssException("URL中不能包含多个http或https协议");
      }

      validateHost(host);
    } catch (URISyntaxException e) {
      throw new BssException("URL格式不正确 : " + url, e);
    }
  }

  private static void validateHost(String host) {
    // 检查主机名
    if (StringUtils.isEmpty(host)) {
      throw new BssException("URL主机名不能为空");
    }

    // 检查是否为本地地址
    if (LOCALHOST.equalsIgnoreCase(host) || LOCAL_IP_PATTERN.matcher(host).matches()) {
      throw new BssException("禁止访问本地地址");
    }

    // 解析IP地址并检查是否为本地地址
    try {
      InetAddress addr = InetAddress.getByName(host);
      String ip = addr.getHostAddress();
      if (LOCAL_IP_PATTERN.matcher(ip).matches()) {
        throw new BssException("禁止访问本地IP地址");
      }
    } catch (UnknownHostException e) {
      // 如果无法解析主机名，允许继续（可能是一个有效的外部域名）
      logger.warn("无法解析主机名: {}", host, e);
    }
  }


  /**
   * 提取文本里面的http/https连接
   *
   * @param text 带连接的文本
   * @return 连接列表
   */
  public static List<String> extractUrls(String text) {
    List<String> urls = new ArrayList<>();
    if (StringUtils.isEmpty(text)) {
      return urls;
    }
    // 使用标准URL模式匹配
    Matcher matcher = URL_PATTERN.matcher(text);
    while (matcher.find()) {
      String url = matcher.group();
      // 清理URL，移除无效字符
      url = cleanUrl(url);
      // 验证URL的基本格式
      if (isValidUrl(url, false)) {
        urls.add(url);
      }
    }
    // 使用localhost专用模式匹配
    Matcher localhostMatcher = LOCALHOST_URL_PATTERN.matcher(text);
    while (localhostMatcher.find()) {
      String url = localhostMatcher.group();
      // 清理URL，移除无效字符
      url = cleanUrl(url);
      // 验证URL的基本格式
      if (isValidUrl(url, true)) {
        urls.add(url);
      }
    }
    // 去重
    return urls.stream().distinct().collect(Collectors.toList());
  }

  /**
   * 清理URL，移除无效的字符
   *
   * @param url 原始URL
   * @return 清理后的URL
   */
  private static String cleanUrl(String url) {
    if (StringUtils.isEmpty(url)) {
      return url;
    }

    // 找到协议结束位置
    int protocolEnd = url.indexOf("://");
    if (protocolEnd == -1) {
      return url;
    }

    protocolEnd += 3; // 跳过 ://

    // 找到路径开始位置
    int pathStart = url.indexOf('/', protocolEnd);
    if (pathStart == -1) {
      // 没有路径，直接返回
      return url;
    }

    // 提取协议和域名部分
    String protocolAndDomain = url.substring(0, pathStart);

    // 提取路径部分
    String pathPart = url.substring(pathStart);

    pathPart = getPathPart(pathPart);

    return protocolAndDomain + pathPart;
  }

  private static String getPathPart(String pathPart) {
    // 在路径部分查找第一个无效字符（非URL有效字符）
    // URL有效字符：字母、数字、-._~:/?#[]@!$&'()*+,;=%

    int invalidCharIndex = -1;
    for (int i = 0; i < pathPart.length(); i++) {
      char c = pathPart.charAt(i);
      if (!(Character.isLetterOrDigit(c) || VALID_URL_CHARS.indexOf(c) >= 0)) {
        invalidCharIndex = i;
        break;
      }
    }

    if (invalidCharIndex != -1) {
      // 截断到第一个无效字符
      pathPart = pathPart.substring(0, invalidCharIndex);
      // 移除末尾的无效字符（如逗号、句号等）

      while (pathPart.length() > 1 && INVALID_END_CHARS.indexOf(pathPart.charAt(pathPart.length() - 1)) >= 0) {
        pathPart = pathPart.substring(0, pathPart.length() - 1);
      }
    }
    return pathPart;
  }

  /**
   * 验证URL的基本格式
   *
   * @param url 待验证的URL
   * @return 是否为有效的URL
   */
  private static boolean isValidUrl(String url, boolean isLocalhost) {
    if (StringUtils.isEmpty(url)) {
      return false;
    }
    // 检查是否以http://或https://开头
    if (!url.startsWith(URL_PREFIX_HTTP) && !url.startsWith(URL_PREFIX_HTTPS)) {
      return false;
    }
    // 检查是否包含有效的域名部分
    String[] parts = url.split(URL_VALID_STR);
    if (parts.length < 2) {
      return false;
    }
    String domainPart = parts[1];
    if (domainPart.isEmpty()) {
      return false;
    }
    if (isLocalhost) {
      // 检查是否为localhost域名
      if (!domainPart.startsWith(URL_LOCALHOST)) {
        return false;
      }
      // 检查localhost域名部分是否包含有效的字符（允许端口号和路径）
      return domainPart.matches(URL_VALID_LOCALHOST_PATTERN);
    } else {
      // 检查域名部分是否包含有效的字符
      return domainPart.matches(URL_VALID_PATTERN);
    }
  }


//  public static void main(String[] args) {
//    // 测试用例
//    String[] testUrls = {
//      // 合法URL
//      "https://www.baidu.com",
//      "http://www.example.com",
//
//      // 非法URL - 协议不支持
//      "ftp://192.168.1.1",
//      "file:///etc/passwd",
//
//      // 非法URL - 本地IP地址
//      "http://127.0.0.1",
//      "http://192.168.1.1",
//      "http://10.0.0.1",
//      "http://172.16.0.1",
//      "http://[::1]/",
//
//      // 非法URL - 多个协议
//      "http://https://www.example.com",
//
//      // 非法URL - 禁用协议关键字
//      "http://example.com/file://etc/passwd",
//
//      // 非法URL - 格式错误
//      "http://",
//      "http://.com",
//    };
//
//    for (String url : testUrls) {
//      try {
//        validateUrl(url);
//        System.out.println("[PASS] URL验证通过: " + url);
//      } catch (Exception e) {
//        System.out.println("[FAIL] URL验证失败: " + url + " 错误: " + e.getMessage());
//      }
//    }
//  }

//  public static void main(String[] args) {
//    //String url = "https://www.chinanews.com.cn/";
//    //String url = "https://v.douyin.com/pqKMp98MLEs/";
//    String url="https://www.iesdouyin.com/share/video/7540864667623689523/?region=CN&amp;mid=7540864691133172506&amp;u_code=11dkj3amj&amp;did=MS4wLjABAAAArh7Z35ROccFU_n-NUegXzKHfct1RXqsQ6Ts94wgugNQ&amp;iid=MS4wLjABAAAA-a0gNLapcPnbw5EgoCxjcjqt_sLt3Z84NNR9CKQ-LpAE50nkaHGkbe4RQgLDLsWY&amp;with_sec_did=1&amp;video_share_track_ver=&amp;titleType=title&amp;share_sign=pYnPAxPTAw8LZZ_hKDbdBBDykbyMRkGb974T9AkYgvA-&amp;share_version=320600&amp;ts=1755821227&amp;from_aid=1128&amp;from_ssr=1&amp;share_track_info=%7B%22link_description_type%22%3A%22%22%7D&amp;utm_source=copy&amp;utm_campaign=client_share&amp;utm_medium=android&amp;app=aweme&amp;activity_info=%7B%22social_author_id%22%3A%223302562925905059%22%2C%22social_share_id%22%3A%2283774363440_1755842510579%22%2C%22social_share_time%22%3A%221755842510%22%2C%22social_share_user_id%22%3A%2283774363440%22%7D&amp;share_extra_params=%7B%22schema_type%22%3A%221%22%7D";
//    String content = WebPageFetchUtil.fetchContent(url);
//    System.out.println(content);
//  }


//  public static void main(String[] args) {
//    System.out.println("=== WebPageFetchUtil URL提取功能测试 ===");
//    System.out.println("测试案例来源：link_extractor_test_cases.txt");
//    System.out.println();
//
//    int totalTests = 0;
//    int passedTests = 0;
//
//    // 测试案例1：基础域名测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】基础域名测试");
//    String test1 = "请访问 http://example.com获取更多信息，或者查看 https://www.example.com的主页，还有子域名 https://subdomain.example.com 也值得关注。";
//    List<String> result1 = extractUrls(test1);
//    List<String> expected1 = Arrays.asList("http://example.com", "https://www.example.com", "https://subdomain.example.com");
//    boolean test1Pass = compareResults(result1, expected1, "基础域名测试");
//    if (test1Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例2：带端口号测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】带端口号测试");
//    String test2 = "本地开发环境请访问 http://localhost:8080，API服务地址是 https://api.example.com:443，测试服务器在 http://test.server:3000。";
//    List<String> result2 = extractUrls(test2);
//    List<String> expected2 = Arrays.asList("http://localhost:8080", "https://api.example.com:443", "http://test.server:3000");
//    boolean test2Pass = compareResults(result2, expected2, "带端口号测试");
//    if (test2Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例3：带路径测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】带路径测试");
//    String test3 = "查看 https://example.com/path 页面，或者访问 http://example.com/path/to/resource 资源，还有 https://example.com/path/with/slashes/ 目录。";
//    List<String> result3 = extractUrls(test3);
//    List<String> expected3 = Arrays.asList("https://example.com/path", "http://example.com/path/to/resource", "https://example.com/path/with/slashes/");
//    boolean test3Pass = compareResults(result3, expected3, "带路径测试");
//    if (test3Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例4：带查询参数测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】带查询参数测试");
//    String test4 = "搜索功能请使用 https://example.com/search?q=keyword，API调用示例 http://example.com/api?param1=value1&param2=value2，页面浏览 https://example.com/page?id=123&category=tech。";
//    List<String> result4 = extractUrls(test4);
//    List<String> expected4 = Arrays.asList("https://example.com/search?q=keyword", "http://example.com/api?param1=value1&param2=value2", "https://example.com/page?id=123&category=tech");
//    boolean test4Pass = compareResults(result4, expected4, "带查询参数测试");
//    if (test4Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例5：带锚点测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】带锚点测试");
//    String test5 = "直接跳转到 https://example.com/page#section1 章节，查看 http://example.com/doc#chapter2 文档，或者访问 https://example.com/blog#comments 评论区域。";
//    List<String> result5 = extractUrls(test5);
//    List<String> expected5 = Arrays.asList("https://example.com/page#section1", "http://example.com/doc#chapter2", "https://example.com/blog#comments");
//    boolean test5Pass = compareResults(result5, expected5, "带锚点测试");
//    if (test5Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例6：复杂组合测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】复杂组合测试");
//    String test6 = "完整的API调用示例：https://api.example.com:8080/v1/users?filter=active&limit=10#results\n本地搜索测试：http://localhost:3000/search?q=java%20tutorial&page=2#top\n复杂子域名调用：https://sub.domain.example.com:443/path/to/api?token=abc123&format=json#response";
//    List<String> result6 = extractUrls(test6);
//    List<String> expected6 = Arrays.asList("https://api.example.com:8080/v1/users?filter=active&limit=10#results", "http://localhost:3000/search?q=java%20tutorial&page=2#top", "https://sub.domain.example.com:443/path/to/api?token=abc123&format=json#response");
//    boolean test6Pass = compareResults(result6, expected6, "复杂组合测试");
//    if (test6Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例7：特殊字符处理测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】特殊字符处理测试（不支特中文）");
//    String test7 = "包含空格的路径：https://example.com/path%20with%20spaces\n搜索查询：http://example.com/search?q=hello%20world\n中文参数：https://example.com/file?name=测试%20文件";
//    List<String> result7 = extractUrls(test7);
//    List<String> expected7 = Arrays.asList("https://example.com/path%20with%20spaces", "http://example.com/search?q=hello%20world", "https://example.com/file?name=测试%20文件");
//    boolean test7Pass = compareResults(result7, expected7, "特殊字符处理测试");
//    if (test7Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例8：混合文本测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】混合文本测试");
//    String test8 = "这是一个包含多种链接格式的段落。你可以访问我们的主站 https://www.example.com，或者查看API文档 https://api.example.com:8080/docs。如果你需要搜索，请使用 https://example.com/search?q=test&page=1#results。本地开发请访问 http://localhost:3000/dashboard。更多资源请访问 http://subdomain.example.com/resources/index.html#section2。";
//    List<String> result8 = extractUrls(test8);
//    List<String> expected8 = Arrays.asList("https://www.example.com", "https://api.example.com:8080/docs", "https://example.com/search?q=test&page=1#results", "http://localhost:3000/dashboard", "http://subdomain.example.com/resources/index.html#section2");
//    boolean test8Pass = compareResults(result8, expected8, "混合文本测试");
//    if (test8Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例9：不支持的格式测试（用于对比）
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】不支持的格式测试（应该为空）");
//    String test9 = "以下格式应该不被提取：无协议域名 www.example.com，FTP协议 ftp://example.com，相对路径 /path/to/resource，邮箱地址 user@example.com。";
//    List<String> result9 = extractUrls(test9);
//    List<String> expected9 = Arrays.asList();
//    boolean test9Pass = compareResults(result9, expected9, "不支持的格式测试");
//    if (test9Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例10：边界情况测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】边界情况测试");
//    String test10a = "";
//    List<String> result10a = extractUrls(test10a);
//    List<String> expected10a = Arrays.asList();
//    boolean test10aPass = compareResults(result10a, expected10a, "空文本测试");
//
//    String test10b = "请访问 www.example.com 和 ftp://test.com 以及 /path/file.html";
//    List<String> result10b = extractUrls(test10b);
//    List<String> expected10b = Arrays.asList();
//    boolean test10bPass = compareResults(result10b, expected10b, "只有无效格式测试");
//
//    String test10c = "有效链接：https://example.com 和 http://localhost:8080/test\n无效链接：www.test.com 和 ftp://ftp.example.com";
//    List<String> result10c = extractUrls(test10c);
//    List<String> expected10c = Arrays.asList("https://example.com", "http://localhost:8080/test");
//    boolean test10cPass = compareResults(result10c, expected10c, "混合有效和无效测试");
//
//    boolean test10Pass = test10aPass && test10bPass && test10cPass;
//    if (test10Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例11：中文环境测试（不支持）
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】中文环境测试（不支特中文）");
//    String test11 = "欢迎访问我们的网站 https://www.example.com，查看中文文档 https://example.com/docs/中文页面#章节一。本地测试环境 http://localhost:8080/中文路径?参数=值#锚点。";
//    List<String> result11 = extractUrls(test11);
//    List<String> expected11 = Arrays.asList("https://www.example.com", "https://example.com/docs/中文页面#章节一", "http://localhost:8080/中文路径?参数=值#锚点");
//    boolean test11Pass = compareResults(result11, expected11, "中文环境测试");
//    if (test11Pass) passedTests++;
//    System.out.println();
//
//    // 测试案例12：重复链接测试
//    totalTests++;
//    System.out.println("【测试 " + totalTests + "】重复链接测试");
//    String test12a = "重复链接：https://example.com https://example.com https://example.com";
//    List<String> result12a = extractUrls(test12a);
//    List<String> expected12a = Arrays.asList("https://example.com");
//    boolean test12aPass = compareResults(result12a, expected12a, "重复链接去重测试");
//
//    String test12b = "相似但不同：https://example.com/page1 https://example.com/page2 https://example.com/page3";
//    List<String> result12b = extractUrls(test12b);
//    List<String> expected12b = Arrays.asList("https://example.com/page1", "https://example.com/page2", "https://example.com/page3");
//    boolean test12bPass = compareResults(result12b, expected12b, "相似链接测试");
//
//    boolean test12Pass = test12aPass && test12bPass;
//    if (test12Pass) passedTests++;
//    System.out.println();
//
//    // 测试结果汇总
//    System.out.println("=== 测试结果汇总 ===");
//    System.out.println("总测试数：" + totalTests);
//    System.out.println("通过测试：" + passedTests);
//    System.out.println("失败测试：" + (totalTests - passedTests));
//    System.out.println("成功率：" + (passedTests * 100.0 / totalTests) + "%");
//
//    if (passedTests == totalTests) {
//      System.out.println("✅ 所有测试通过！URL提取功能正常。");
//    } else {
//      System.out.println("❌ 有测试失败，请检查上述失败案例。");
//    }
//  }
//
//  /**
//   * 比较测试结果与预期结果
//   *
//   * @param actual   实际结果
//   * @param expected 预期结果
//   * @param testName 测试名称
//   * @return 是否通过测试
//   */
//  private static boolean compareResults(List<String> actual, List<String> expected, String testName) {
//    System.out.println("测试内容：" + testName);
//    System.out.println("实际结果：" + actual);
//    System.out.println("预期结果：" + expected);
//
//    if (actual.size() != expected.size()) {
//      System.out.println("❌ 测试失败：数量不匹配（实际：" + actual.size() + "，预期：" + expected.size() + "）");
//      return false;
//    }
//
//    for (String url : expected) {
//      if (!actual.contains(url)) {
//        System.out.println("❌ 测试失败：缺少预期URL " + url);
//        return false;
//      }
//    }
//
//    for (String url : actual) {
//      if (!expected.contains(url)) {
//        System.out.println("❌ 测试失败：包含非预期URL " + url);
//        return false;
//      }
//    }
//
//    System.out.println("✅ 测试通过");
//    return true;
//  }
}
