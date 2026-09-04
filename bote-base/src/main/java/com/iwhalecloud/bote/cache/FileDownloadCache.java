package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.base.FileDownloadToken;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 文档缓存管理器
 * 提供文件预览服务的临时token生成和校验功能
 *
 * @author Aiqing
 * @since 2025/9/6
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class FileDownloadCache {

  private static final Logger logger = LoggerFactory.getLogger(FileDownloadCache.class);

  /**
   * Token缓存key前缀
   */
  private static final String FILE_DOWNLOAD_TOKEN_PREFIX = "file_download_token:";
  /**
   * Token默认过期时间（分钟）
   */
  private static final int DEFAULT_TOKEN_EXPIRE_MINUTES = 5;
  private final ICacheClient cacheClient;

  public FileDownloadCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_DOC,
      CacheConsts.CACHE_DOCUMENT_DOWNLOAD_PREFIX);
  }

  /**
   * 生成文件下载Token（带自定义过期时间）
   *
   * @param userId 用户ID
   * @param documentId 文档ID
   * @param fileId 文件ID
   * @param fileName 文件名称
   * @param fileSize 文件大小
   * @return 生成的token
   */
  public FileDownloadToken generateFileDownloadToken(Long userId,
                                                     String documentId,
                                                     Long fileId,
                                                     String fileName,
                                                     Long fileSize,
                                                     Function<String, String> urlBuilder) {

    // 参数校验
    if (userId == null || StringUtils.isBlank(documentId)) {
      throw new IllegalArgumentException("用户ID、文档ID和下载URL不能为空");
    }
    // 生成唯一token
    String token = UUIDUtils.randomFormatUuid();

    // 创建token对象
    LocalDateTime now = LocalDateTime.now();
    FileDownloadToken tokenInfo = new FileDownloadToken();
    tokenInfo.setToken(token);
    tokenInfo.setUserId(userId);
    tokenInfo.setDocumentId(documentId);
    tokenInfo.setFileId(fileId);
    tokenInfo.setFileName(fileName);
    tokenInfo.setFileSize(fileSize);
    tokenInfo.setCreateTime(now);
    tokenInfo.setExpireTime(now.plusMinutes(DEFAULT_TOKEN_EXPIRE_MINUTES));

    // 构造下载URL
    String downloadUrl = urlBuilder.apply(token);
    tokenInfo.setDownloadUrl(downloadUrl);

    // 存储到缓存
    String cacheKey = FILE_DOWNLOAD_TOKEN_PREFIX + token;
    String tokenJson = JsonUtil.toJsonString(tokenInfo);

    // 设置缓存过期时间
    cacheClient.opsForValue().set(cacheKey, tokenJson, DEFAULT_TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

    logger.trace("生成文件下载Token成功: token={}, userId={}, documentId={}, expireMinutes={}",
      token, userId, documentId, DEFAULT_TOKEN_EXPIRE_MINUTES);

    return tokenInfo;
  }

  /**
   * 校验并获取Token信息
   *
   * @param token token字符串
   * @return Token信息，如果无效返回null
   */
  @Nullable
  public FileDownloadToken validateAndGetToken(String token) {
    if (StringUtils.isBlank(token)) {
      return null;
    }
    String cacheKey = FILE_DOWNLOAD_TOKEN_PREFIX + token;
    String tokenJson = cacheClient.opsForValue().get(cacheKey);

    if (StringUtils.isBlank(tokenJson)) {
      logger.debug("Token不存在或已过期: {}", token);
      return null;
    }
    try {
      FileDownloadToken tokenInfo = JsonUtil.parseJson(tokenJson, FileDownloadToken.class);
      if (tokenInfo == null) {
        logger.warn("Token解析失败: {}", token);
        return null;
      }
      // 检查token是否有效
      if (!tokenInfo.isValid()) {
        logger.debug("Token无效: token={}, expired={}", token, tokenInfo.isExpired());
        return null;
      }
      return tokenInfo;
    }
    catch (Exception e) {
      logger.error("Token校验异常: token={}, error={}", token, e.getMessage(), e);
      return null;
    }
  }

  /**
   * 获取Token信息（不校验有效性）
   *
   * @param token token字符串
   * @return Token信息，如果不存在返回null
   */
  @Nullable
  public FileDownloadToken getTokenInfo(String token) {
    if (StringUtils.isBlank(token)) {
      return null;
    }

    String cacheKey = FILE_DOWNLOAD_TOKEN_PREFIX + token;
    String tokenJson = cacheClient.opsForValue().get(cacheKey);

    if (StringUtils.isBlank(tokenJson)) {
      return null;
    }

    try {
      return JsonUtil.parseJson(tokenJson, FileDownloadToken.class);
    }
    catch (Exception e) {
      logger.error("获取Token信息异常: token={}, error={}", token, e.getMessage(), e);
      return null;
    }
  }
}
