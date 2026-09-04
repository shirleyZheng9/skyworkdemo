package com.iwhalecloud.bote.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpCacheUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

/**
 * 图标缓存抽象类
 *
 * @author bianjp
 * @since 2025-06-16
 */
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractIconCache implements TenantCacheMarker {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** 图标 digest 缓存, key 为 (tenantId, id) */
  private final LoadingCache<@NonNull Pair<Long, Long>, @NonNull String> digestCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterAccess(Duration.ofHours(6))
    .build(new CacheLoader<>() {
      @Override
      public String load(Pair<Long, Long> key) {
        return loadIconDigest(key);
      }
    });
  /** 图标内容缓存，内容可能较大，不宜缓存太久。key 为图标的 digest (很多智能应用/智能体的图标相同，要避免相同的图标缓存多份数据）, value 为 (mediaType, content) */
  private final Cache<@NonNull String, @NonNull Pair<String, byte[]>> contentCache = CacheBuilder.newBuilder()
    .maximumSize(200)
    .expireAfterAccess(Duration.ofHours(1))
    // 允许内存不足时提前释放内存
    .softValues()
    .build();

  /** 图标名称，用于构造错误信息 */
  protected final String iconName;
  /** 默认图标 */
  protected final Resource defaultIcon;
  /** 默认图标摘要 */
  protected String defaultIconDigest;
  /** 默认图标大小 */
  protected int defaultIconLength;

  @SuppressFBWarnings({"WEAK_MESSAGE_DIGEST_MD5", "CT_CONSTRUCTOR_THROW"})
  protected AbstractIconCache(String iconName, @Nullable Resource defaultIcon) {
    this.iconName = iconName;
    if (defaultIcon != null) {
      this.defaultIcon = defaultIcon;
      try (InputStream inputStream = defaultIcon.getInputStream()) {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        this.defaultIconDigest = DigestUtils.md5Hex(bytes);
        this.defaultIconLength = bytes.length;
      }
      catch (Exception e) {
        throw new IllegalStateException("读取默认图标失败", e);
      }
    }
    else {
      this.defaultIcon = null;
      this.defaultIconDigest = null;
      this.defaultIconLength = 0;
    }
  }

  /**
   * 发送图标
   *
   * @param tenantId 租户 ID
   * @param id 智能应用/智能体 ID
   * @param request 请求
   * @param response 响应
   */
  public void sendIcon(Long tenantId, Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
    // 获取图标的 digest
    String digest;
    try {
      digest = getDigest(tenantId, id);
    }
    catch (BssException e) {
      HttpCacheUtil.sendError(response, e);
      return;
    }

    // 未配置图标时发送默认图标
    if (digest.isEmpty()) {
      sendDefaultIcon(request, response);
      return;
    }
    // 发送缓存响应头
    if (HttpCacheUtil.sendCacheHeader(request, response, digest)) {
      return;
    }

    // 获取图标内容
    Pair<String, byte[]> icon;
    try {
      icon = getContent(tenantId, id, digest);
    }
    catch (BssException e) {
      HttpCacheUtil.sendError(response, e);
      return;
    }
    // 前面的 digest 已经检查了是否未配置图标，这里不应该出现为空，以防万一还是检查下
    if (icon.getLeft() == null) {
      HttpCacheUtil.sendError(response, "未配置图标");
      return;
    }
    response.setContentType(icon.getLeft());
    response.setContentLength(icon.getRight().length);
    response.getOutputStream().write(icon.getRight());
  }

  /**
   * 发送默认图标
   */
  private void sendDefaultIcon(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (defaultIcon == null) {
      response.sendError(HttpStatus.NOT_FOUND.value(), "未配置图标");
      return;
    }
    // 发送缓存响应头
    if (HttpCacheUtil.sendCacheHeader(request, response, defaultIconDigest)) {
      return;
    }
    response.setContentType(MediaType.IMAGE_PNG_VALUE);
    response.setContentLength(defaultIconLength);
    try (InputStream inputStream = defaultIcon.getInputStream()) {
      IOUtils.copy(inputStream, response.getOutputStream());
    }
  }

  /**
   * 获取图标的 digest
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  private String getDigest(Long tenantId, Long id) {
    try {
      return digestCache.get(Pair.of(tenantId, id));
    }
    catch (ExecutionException | UncheckedExecutionException e) {
      if (e.getCause() instanceof BssException) {
        throw (BssException) e.getCause();
      }
      logger.warn("Failed to get icon digest: tenantId={}, id={}", tenantId, id, e.getCause());
      throw new BssException(String.format("查询%s失败: id=%d, error=%s", iconName, id, ExpUtil.getMsg(e.getCause())));
    }
    catch (Exception e) {
      logger.warn("Failed to get icon digest: tenantId={}, id={}", tenantId, id, e);
      throw new BssException(String.format("查询%s失败: id=%d, error=%s", iconName, id, ExpUtil.getMsg(e)));
    }
  }

  /**
   * 获取图标内容
   *
   * @return 图标内容，左边为 mediaType，右边为文件内容
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  private Pair<String, byte[]> getContent(Long tenantId, Long id, String digest) {
    try {
      return contentCache.get(digest, () -> loadIcon(tenantId, id));
    }
    catch (ExecutionException | UncheckedExecutionException e) {
      if (e.getCause() instanceof BssException) {
        throw (BssException) e.getCause();
      }
      logger.warn("Failed to get icon content: tenantId={}, id={}", tenantId, id, e.getCause());
      throw new BssException(String.format("查询%s失败: id=%d, error=%s", iconName, id, ExpUtil.getMsg(e.getCause())));
    }
    catch (Exception e) {
      logger.warn("Failed to get icon content: tenantId={}, id={}", tenantId, id, e);
      throw new BssException(String.format("查询%s失败: id=%d, error=%s", iconName, id, ExpUtil.getMsg(e)));
    }
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  public void refreshLocalCache() {
    logger.debug("Refresh local: cacheName={}", getCacheName());
    digestCache.invalidateAll();
    contentCache.invalidateAll();
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    logger.debug("Refresh local by keys: cacheName={}, keys={}", getCacheName(), keys);
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    invalidateLocalCache(digestCache, keys);
    // 不需要刷新图标内容缓存(key 为 digest, 与 value 必然一致)
  }

  @Override
  public void refresh() {
    refreshLocalCache();
  }

  @Override
  public void refresh(List<String> keys) {
    refreshLocalCache(keys);
  }

  /**
   * 加载图标的 digest
   */
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  private String loadIconDigest(Pair<Long, Long> key) {
    Pair<String, byte[]> info = loadIcon(key.getLeft(), key.getRight());
    // 未配置图标时返回空字符串
    if (info.getLeft() == null) {
      return "";
    }
    String digest = DigestUtils.md5Hex(info.getRight());
    // 顺便设置图标内容缓存，避免获取图标内容时重复查询数据库
    contentCache.put(digest, Pair.of(info.getLeft(), info.getRight()));
    return digest;
  }

  /**
   * 从数据库查询图标
   *
   * @param tenantId 租户 ID
   * @param id 智能应用/智能体 ID
   * @return 图标内容，左边为 mediaType，右边为文件内容
   */
  protected Pair<String, byte[]> loadIcon(Long tenantId, Long id) {
    String icon = loadIconContent(tenantId, id);
    return parseBase64Icon(id, icon);
  }

  /**
   * 解析 base64 形式的图标
   *
   * @param id 智能应用/智能体 ID
   * @param icon base64 形式的图标内容
   */
  protected final Pair<String, byte[]> parseBase64Icon(Long id, String icon) {
    // 未配置图标
    if (StringUtils.isEmpty(icon)) {
      return Pair.of(null, null);
    }
    if (!icon.startsWith("data:image/") || !icon.contains(";base64,")) {
      throw new BssException(iconName + "不合法: id=" + id);
    }
    String mediaType = icon.substring("data:".length(), icon.indexOf(";"));
    String base64 = icon.substring(icon.indexOf(";base64,") + ";base64,".length());
    byte[] content;
    try {
      content = Base64.getDecoder().decode(base64);
    }
    catch (IllegalArgumentException e) {
      throw new BssException(iconName + "不合法: id=" + id, e);
    }
    return Pair.of(mediaType, content);
  }

  /**
   * 从数据库查询图标内容
   *
   * @param tenantId 租户 ID
   * @param id 智能应用/智能体 ID
   * @return 图标内容的 data URI 形式。未配置图标时返回空字符串
   */
  protected abstract String loadIconContent(Long tenantId, Long id);

}
