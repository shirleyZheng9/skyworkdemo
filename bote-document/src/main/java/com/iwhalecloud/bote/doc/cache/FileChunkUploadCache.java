package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.base.dto.FileChunkInfo;
import com.iwhalecloud.bote.doc.module.base.dto.FileChunkUploadCacheDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 文件分片上传缓存 V2 基于BaseSecondaryCache框架实现，使用FileChunkUploadCacheEntity作为泛型类型 以分片批次号为key存储分片信息
 *
 * @author yangran
 * @since 2025-08-25
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class FileChunkUploadCache extends BaseSecondaryCache<FileChunkUploadCacheDTO> {

  private static final Logger logger = LoggerFactory.getLogger(FileChunkUploadCache.class);

  private static final String CHUNK_BATCH_KEY_PREFIX = "chunk_batch:";

  public FileChunkUploadCache() {
    super(DocCacheConsts.GROUP_DOC, DocCacheConsts.CACHE_NAME_FILE_CHUNK_UPLOAD);
    super.useLocalCache = false;
  }

  /**
   * 保存分片详细信息到缓存
   * 使用HSET存储每个分片信息，避免并发覆盖导致数据丢失
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileChunkInfo 分片详细信息
   * @param expireHours 过期时间（小时）
   */
  public void saveChunkInfoWithDetails(Long tenantId, Long userId, FileChunkInfo fileChunkInfo, int expireHours) {
    if (fileChunkInfo.getChunkIndex() == null) {
      logger.warn("保存分片信息参数不完整: tenantId={}, userId={}, chunkIndex=null", tenantId, userId);
      return;
    }

    String batchKey = buildBatchKeyWithTenant(tenantId, userId, fileChunkInfo.getFileHash());
    String chunkField = String.valueOf(fileChunkInfo.getChunkIndex());
    String chunkHashKey = buildChunkHashKey(batchKey);
    // 使用HSET直接保存分片信息，避免并发覆盖
    // HSET操作是原子的，即使没有锁也不会丢失数据
    try {
      String chunkJson = JsonUtil.toJsonString(fileChunkInfo);
      cacheClient.opsForHash().put(chunkHashKey, chunkField, chunkJson);
      // 设置过期时间
      cacheClient.expire(chunkHashKey, expireHours * 3600L, TimeUnit.SECONDS);
      logger.debug("保存分片信息成功: batchKey={}, chunkIndex={}", batchKey, fileChunkInfo.getChunkIndex());
    }
    catch (Exception e) {
      logger.error("保存分片信息失败: batchKey={}, chunkIndex={}, error={}", batchKey, fileChunkInfo.getChunkIndex(), e.getMessage(), e);
      throw new BssException("保存分片信息失败: " + e.getMessage(), e);
    }
  }

  /**
   * 检查分片批次是否完成
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileHash 文件哈希
   * @return 是否完成
   */
  public boolean isChunkBatchCompleted(Long tenantId, Long userId, String fileHash) {
    if (StringUtils.isBlank(fileHash)) {
      return false;
    }
    String batchKey = buildBatchKeyWithTenant(tenantId, userId, fileHash);
    FileChunkUploadCacheDTO entity = get(batchKey);
    if (entity == null) {
      return false;
    }
    return entity.getCompleted() != null && entity.getCompleted();
  }

  /**
   * 获取已上传的分片索引列表
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileHash 文件哈希
   * @return 已上传的分片索引列表
   */
  public List<Integer> getUploadedChunkIndexes(Long tenantId, Long userId, String fileHash) {
    if (StringUtils.isBlank(fileHash)) {
      return Collections.emptyList();
    }
    String batchKey = buildBatchKeyWithTenant(tenantId, userId, fileHash);
    String chunkHashKey = buildChunkHashKey(batchKey);

    // 从HSET中获取所有分片索引
    try {
      Map<Object, Object> chunkMap = cacheClient.opsForHash().entries(chunkHashKey);
      if (chunkMap.isEmpty()) {
        return Collections.emptyList();
      }

      return chunkMap.keySet().stream()
        .map(key -> {
          try {
            return Integer.valueOf(key.toString());
          }
          catch (NumberFormatException e) {
            logger.warn("分片索引格式错误: key={}", key);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .sorted()
        .collect(java.util.stream.Collectors.toList());
    }
    catch (Exception e) {
      logger.warn("获取已上传分片索引列表失败: batchKey={}, error={}", batchKey, e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * 标记分片批次完成
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileHash 文件哈希
   * @param fileUrl 文件URL
   * @param expireHours 过期时间（小时）
   */
  public void markChunkBatchCompleted(Long tenantId, Long userId, String fileHash, String fileUrl, int expireHours) {
    if (StringUtils.isBlank(fileHash)) {
      return;
    }
    String batchKey = buildBatchKeyWithTenant(tenantId, userId, fileHash);

    // 获取现有数据
    FileChunkUploadCacheDTO entity = get(batchKey);
    if (entity == null) {
      entity = new FileChunkUploadCacheDTO();
    }

    // 标记完成状态
    entity.markCompleted(fileUrl);

    // 更新框架缓存
    put(batchKey, entity, expireHours * 3600);
  }

  /**
   * 删除分片批次状态信息
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileHash 文件hash
   */
  public void deleteChunkBatchStatus(Long tenantId, Long userId, String fileHash) {
    if (StringUtils.isBlank(fileHash)) {
      return;
    }
    String batchKey = buildBatchKeyWithTenant(tenantId, userId, fileHash);
    String chunkHashKey = buildChunkHashKey(batchKey);

    try {
      // 删除HSET中的所有分片信息
      cacheClient.delete(chunkHashKey);
      // 删除批次主键（含 completed/fileUrl），避免合并后 check 仍返回 completed 导致前端不再调 merge/上传
      super.delete(batchKey);
      logger.debug("删除分片批次状态信息成功: batchKey={}", batchKey);
    }
    catch (Exception e) {
      logger.warn("删除分片批次状态信息失败: batchKey={}, error={}", batchKey, e.getMessage());
    }
  }

  /**
   * 获取文件URL
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileHash 文件哈希
   * @return 文件URL
   */
  @Nullable
  public String getFileUrl(Long tenantId, Long userId, String fileHash) {
    if (StringUtils.isBlank(fileHash)) {
      return null;
    }
    String batchKey = buildBatchKeyWithTenant(tenantId, userId, fileHash);
    FileChunkUploadCacheDTO entity = get(batchKey);
    if (entity == null) {
      return null;
    }
    return entity.getFileUrl();
  }

  /**
   * 获取所有分片详细信息
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileHash 文件哈希
   * @return 分片详细信息列表
   */
  public List<FileChunkInfo> getAllChunkDetails(Long tenantId, Long userId, String fileHash) {
    if (StringUtils.isBlank(fileHash)) {
      return Collections.emptyList();
    }
    String batchKey = buildBatchKeyWithTenant(tenantId, userId, fileHash);
    String chunkHashKey = buildChunkHashKey(batchKey);

    // 从HSET中获取所有分片信息
    try {
      Map<Object, Object> chunkMap = cacheClient.opsForHash().entries(chunkHashKey);
      if (chunkMap.isEmpty()) {
        return Collections.emptyList();
      }

      return chunkMap.values().stream()
        .map(value -> {
          try {
            return JsonUtil.parseJson(value.toString(), FileChunkInfo.class);
          }
          catch (Exception e) {
            logger.warn("解析分片信息失败: value={}, error={}", value, e.getMessage());
            return null;
          }
        })
        .filter(Objects::nonNull)
        .sorted((c1, c2) -> {
          if (c1.getChunkIndex() == null || c2.getChunkIndex() == null) {
            return 0;
          }
          return c1.getChunkIndex().compareTo(c2.getChunkIndex());
        })
        .collect(java.util.stream.Collectors.toList());
    }
    catch (Exception e) {
      logger.warn("获取所有分片详细信息失败: batchKey={}, error={}", batchKey, e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * 检查分片是否已存在
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileHash 文件哈希
   * @param chunkIndex 分片索引
   * @return 是否存在
   */
  public boolean hasChunk(Long tenantId, Long userId, String fileHash, @Nullable Integer chunkIndex) {
    if (StringUtils.isBlank(fileHash) || chunkIndex == null) {
      return false;
    }
    String batchKey = buildBatchKeyWithTenant(tenantId, userId, fileHash);
    String chunkHashKey = buildChunkHashKey(batchKey);
    String chunkField = String.valueOf(chunkIndex);

    // 从HSET中检查分片是否存在
    try {
      return cacheClient.opsForHash().hasKey(chunkHashKey, chunkField);
    }
    catch (Exception e) {
      logger.warn("检查分片是否存在失败: batchKey={}, chunkIndex={}, error={}", batchKey, chunkIndex, e.getMessage());
      return false;
    }
  }

  /**
   * 获取分片数量
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileHash 文件哈希
   * @return 分片数量
   */
  public long getChunkCount(Long tenantId, Long userId, String fileHash) {
    if (StringUtils.isBlank(fileHash)) {
      return 0;
    }
    String batchKey = buildBatchKeyWithTenant(tenantId, userId, fileHash);
    String chunkHashKey = buildChunkHashKey(batchKey);

    // 从HSET中获取分片数量
    try {
      return cacheClient.opsForHash().size(chunkHashKey);
    }
    catch (Exception e) {
      logger.warn("获取分片数量失败: batchKey={}, error={}", batchKey, e.getMessage());
      return 0;
    }
  }

  /**
   * 构建包含租户ID的批次键
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param fileHash 文件哈希
   * @return 批次键
   */
  private String buildBatchKeyWithTenant(Long tenantId, Long userId, String fileHash) {
    return CHUNK_BATCH_KEY_PREFIX + tenantId + ":" + userId + ":" + fileHash;
  }

  /**
   * 构建分片Hash键（用于HSET存储）
   *
   * @param batchKey 批次键
   * @return 分片Hash键
   */
  private String buildChunkHashKey(String batchKey) {
    return batchKey + ":chunks";
  }
}
