package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.base.dto.FolderUploadCacheDTO;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 文件夹上传缓存 V2
 * 基于BaseSecondaryCache框架实现，使用FolderUploadCacheDTO作为泛型类型
 * 以任务ID为key存储文件夹上传状态信息
 *
 * @author yangran
 * @since 2025-08-29
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class FolderUploadCache extends BaseSecondaryCache<FolderUploadCacheDTO> {

  private static final Logger logger = LoggerFactory.getLogger(FolderUploadCache.class);

  private static final String FOLDER_UPLOAD_KEY_PREFIX = "folder_upload:";

  public FolderUploadCache() {
    super(DocCacheConsts.GROUP_DOC, DocCacheConsts.CACHE_NAME_FOLDER_UPLOAD);
    // 配置缓存策略
    super.useLocalCache = false;
  }

  /**
   * 获取上传任务状态
   *
   * @param taskId 任务ID
   * @return 上传状态DTO
   */
  @Nullable
  public FolderUploadCacheDTO getUploadStatus(String taskId) {
    if (StringUtils.isBlank(taskId)) {
      return null;
    }

    String cacheKey = buildTaskKey(taskId);
    return get(cacheKey);
  }

  /**
   * 暂停上传任务
   *
   * @param taskId 任务ID
   * @param expireHours 过期时间（小时）
   */
  public void pauseUploadTask(String taskId, int expireHours) {
    if (StringUtils.isBlank(taskId)) {
      return;
    }

    String cacheKey = buildTaskKey(taskId);
    FolderUploadCacheDTO status = get(cacheKey);
    if (status != null) {
      status.pauseUpload();
      put(cacheKey, status, expireHours * 3600);
    }
  }

  /**
   * 恢复上传任务
   *
   * @param taskId 任务ID
   * @param expireHours 过期时间（小时）
   */
  public void resumeUploadTask(String taskId, int expireHours) {
    if (StringUtils.isBlank(taskId)) {
      return;
    }

    String cacheKey = buildTaskKey(taskId);
    FolderUploadCacheDTO status = get(cacheKey);
    if (status != null) {
      status.resumeUpload();
      put(cacheKey, status, expireHours * 3600);
    }
  }

  /**
   * 取消上传任务
   *
   * @param taskId 任务ID
   * @param expireHours 过期时间（小时）
   */
  public void cancelUploadTask(String taskId, int expireHours) {
    if (StringUtils.isBlank(taskId)) {
      return;
    }

    String cacheKey = buildTaskKey(taskId);
    FolderUploadCacheDTO status = get(cacheKey);
    if (status != null) {
      status.cancelUpload();
      put(cacheKey, status, expireHours * 3600);
    }
  }

  /**
   * 获取文件夹上传缓存（兼容旧方法名）
   *
   * @param taskId 任务ID
   * @return 文件夹上传缓存DTO
   */
  @Nullable
  public FolderUploadCacheDTO getFolderUploadCache(String taskId) {
    return getUploadStatus(taskId);
  }

  /**
   * 保存文件夹上传缓存
   *
   * @param cache 文件夹上传缓存DTO
   */
  public void saveFolderUploadCache(FolderUploadCacheDTO cache) {
    if (StringUtils.isBlank(cache.getTaskId())) {
      return;
    }

    String cacheKey = buildTaskKey(cache.getTaskId());
    put(cacheKey, cache, 2 * 60 * 60); // 2小时过期
  }

  /**
   * 使用HSET保存文件信息，避免并发覆盖
   *
   * @param taskId 任务ID
   * @param fileHash 文件哈希
   * @param fileInfo 文件信息
   * @param expireHours 过期时间（小时）
   */
  public void saveFileInfoWithHash(String taskId, String fileHash, FolderUploadCacheDTO.UploadedFileInfo fileInfo, int expireHours) {
    if (StringUtils.isBlank(taskId) || StringUtils.isBlank(fileHash)) {
      logger.warn("保存文件信息参数不完整: taskId={}, fileHash={}", taskId, fileHash);
      return;
    }

    String fileHashKey = buildFileHashKey(taskId);

    // 使用HSET直接保存文件信息，避免并发覆盖
    try {
      String fileInfoJson = JsonUtil.toJsonString(fileInfo);
      cacheClient.opsForHash().put(fileHashKey, fileHash, fileInfoJson);
      // 设置过期时间
      cacheClient.expire(fileHashKey, expireHours * 3600L, TimeUnit.SECONDS);
      logger.debug("保存文件信息成功: taskId={}, fileHash={}", taskId, fileHash);
    }
    catch (Exception e) {
      logger.error("保存文件信息失败: taskId={}, fileHash={}, error={}", taskId, fileHash, e.getMessage(), e);
      throw new com.iwhalecloud.bss.litchi.base.exception.BssException("保存文件信息失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取所有文件信息
   *
   * @param taskId 任务ID
   * @return 文件信息列表
   */
  public List<FolderUploadCacheDTO.UploadedFileInfo> getAllFileInfos(String taskId) {
    if (StringUtils.isBlank(taskId)) {
      return new ArrayList<>();
    }

    String fileHashKey = buildFileHashKey(taskId);

    // 从HSET中获取所有文件信息
    try {
      Map<Object, Object> fileMap = cacheClient.opsForHash().entries(fileHashKey);
      if (fileMap.isEmpty()) {
        return new ArrayList<>();
      }

      List<FolderUploadCacheDTO.UploadedFileInfo> fileInfos = new ArrayList<>();
      for (Object value : fileMap.values()) {
        try {
          FolderUploadCacheDTO.UploadedFileInfo fileInfo = JsonUtil.parseJson(value.toString(),
            FolderUploadCacheDTO.UploadedFileInfo.class);
          if (fileInfo != null) {
            fileInfos.add(fileInfo);
          }
        }
        catch (Exception e) {
          logger.warn("解析文件信息失败: value={}, error={}", value, e.getMessage());
        }
      }
      return fileInfos;
    }
    catch (Exception e) {
      logger.warn("获取所有文件信息失败: taskId={}, error={}", taskId, e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * 检查文件信息是否存在
   *
   * @param taskId 任务ID
   * @param fileHash 文件哈希
   * @return 是否存在
   */
  public boolean hasFileInfo(String taskId, String fileHash) {
    if (StringUtils.isBlank(taskId) || StringUtils.isBlank(fileHash)) {
      return false;
    }

    String fileHashKey = buildFileHashKey(taskId);

    // 从HSET中检查文件是否存在
    try {
      return cacheClient.opsForHash().hasKey(fileHashKey, fileHash);
    }
    catch (Exception e) {
      logger.warn("检查文件信息是否存在失败: taskId={}, fileHash={}, error={}", taskId, fileHash, e.getMessage());
      return false;
    }
  }

  /**
   * 构建任务键
   *
   * @param taskId 任务ID
   * @return 缓存键
   */
  private String buildTaskKey(String taskId) {
    return FOLDER_UPLOAD_KEY_PREFIX + taskId;
  }

  /**
   * 构建文件信息Hash键（用于HSET存储）
   *
   * @param taskId 任务ID
   * @return 文件信息Hash键
   */
  private String buildFileHashKey(String taskId) {
    return FOLDER_UPLOAD_KEY_PREFIX + taskId + ":files";
  }
}
