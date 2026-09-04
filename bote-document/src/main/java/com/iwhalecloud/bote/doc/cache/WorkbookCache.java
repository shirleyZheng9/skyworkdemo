package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockStatusDTO;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 在线表格相关缓存
 *
 * @author Aiqing
 * @since 2025/9/26
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class WorkbookCache {

  private static final Logger logger = LoggerFactory.getLogger(WorkbookCache.class);

  private final ICacheClient cacheClient;
  private final IDcUserService dcUserService;

  public WorkbookCache(CacheFactory cacheFactory, IDcUserService dcUserService) {
    this.cacheClient = cacheFactory.getCacheClient(DocCacheConsts.GROUP_DOC,
      DocCacheConsts.CACHE_PREFIX_WORKBOOK);
    this.dcUserService = dcUserService;
  }

  /**
   * 获取锁定状态
   *
   * @param documentId 文档ID
   * @param currentUserId 当前用户ID
   * @param currentSessionId 当前会话ID
   * @return 锁定状态
   */
  public WorkbookLockStatusDTO getLockStatus(String documentId, Long currentUserId, @Nullable String currentSessionId) {
    String lockKey = buildLockKey(documentId);
    String lockInfo = cacheClient.opsForValue().get(lockKey);

    if (StringUtils.isBlank(currentSessionId)) {
      currentSessionId = String.valueOf(currentUserId);
    }

    WorkbookLockStatusDTO status = new WorkbookLockStatusDTO();
    status.setDocumentId(documentId);

    if (StringUtils.isEmpty(lockInfo)) {
      // 未锁定
      status.setLocked(false);
      status.setCanEdit(true);
      return status;
    }
    try {
      WorkbookLockInfoDTO lockData = JsonUtil.parseJson(lockInfo, WorkbookLockInfoDTO.class);
      // 检查是否已过期
      if (lockData == null || lockData.isExpired()) {
        // 锁定已过期，清除缓存
        cacheClient.delete(lockKey);
        status.setLocked(false);
        status.setCanEdit(true);
        return status;
      }

      status.setLocked(true);
      status.setLockUserId(lockData.getUserId());
      status.setLockSessionId(lockData.getSessionId());
      status.setLockTime(lockData.getLockTimeAsDate());
      status.setExpireTime(lockData.getExpireTimeAsDate());

      // 填充锁定用户名称
      if (lockData.getUserId() != null) {
        PortalUserDTO lockUser = dcUserService.findUserById(lockData.getUserId());
        if (lockUser != null) {
          status.setLockUserName(lockUser.getUserName());
        }
      }
      // 判断当前用户会话是否可以编辑（需要用户ID和会话ID都匹配）
      status.setCanEdit(lockData.isLockedByUserSession(currentUserId, currentSessionId));
      return status;
    }
    catch (Exception e) {
      logger.error("解析锁定信息失败: {}", lockInfo, e);
      // 解析失败时清除缓存
      cacheClient.delete(lockKey);
      status.setLocked(false);
      status.setCanEdit(true);
      return status;
    }
  }

  /**
   * 锁定文档
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @param sessionId 会话ID
   * @param durationMinutes 锁定时长（分钟）
   * @return 锁定状态
   * @throws RuntimeException 如果文档已被其他用户或会话锁定
   */
  public WorkbookLockStatusDTO lockDocument(String documentId, Long userId, String sessionId, Integer durationMinutes) {
    String lockKey = buildLockKey(documentId);

    // 检查当前锁定状态
    WorkbookLockStatusDTO currentStatus = getLockStatus(documentId, userId, sessionId);
    if (Boolean.TRUE.equals(currentStatus.getLocked())) {
      // 检查是否是当前用户的当前会话锁定
      if (!Objects.equals(currentStatus.getLockUserId(), userId) ||
        !Objects.equals(currentStatus.getLockSessionId(), sessionId)) {
        // 已被其他用户或其他会话锁定
        logger.warn("文档锁定冲突 - 文档ID: {}, 请求用户: {}, 请求会话: {}, 锁定用户: {}, 锁定会话: {}",
          documentId, userId, sessionId, currentStatus.getLockUserId(), currentStatus.getLockSessionId());

        currentStatus.setCanEdit(false);
        return currentStatus;
      }
    }
    // 创建或更新锁定信息
    WorkbookLockInfoDTO lockData = new WorkbookLockInfoDTO(userId, sessionId, durationMinutes);
    String lockInfo = JsonUtil.toJsonString(lockData);
    cacheClient.opsForValue().set(lockKey, lockInfo, durationMinutes, TimeUnit.MINUTES);

    // 返回锁定状态
    return getLockStatus(documentId, userId, sessionId);
  }

  /**
   * 解锁文档
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @param sessionId 会话ID
   * @return 锁定状态
   */
  public WorkbookLockStatusDTO unlockDocument(String documentId, Long userId, String sessionId) {
    String lockKey = buildLockKey(documentId);

    // 检查当前锁定状态
    WorkbookLockStatusDTO currentStatus = getLockStatus(documentId, userId, sessionId);
    if (!Boolean.TRUE.equals(currentStatus.getLocked())) {
      // 未锁定，直接返回
      return currentStatus;
    }
    if (!Objects.equals(currentStatus.getLockUserId(), userId) ||
      !Objects.equals(currentStatus.getLockSessionId(), sessionId)) {
      // 不是当前用户会话锁定的，不能解锁
      logger.warn("文档解锁失败 - 权限不足，文档ID: {}, 请求用户: {}, 请求会话: {}, 锁定用户: {}, 锁定会话: {}",
        documentId, userId, sessionId, currentStatus.getLockUserId(), currentStatus.getLockSessionId());
      return currentStatus;
    }
    // 删除锁定信息
    cacheClient.delete(lockKey);
    logger.info("文档解锁成功 - 文档ID: {}, 用户ID: {}, 会话ID: {}", documentId, userId, sessionId);
    // 返回解锁后的状态
    return getLockStatus(documentId, userId, sessionId);
  }


  /**
   * 构建锁定缓存键
   */
  private String buildLockKey(String documentId) {
    return "lock:" + documentId;
  }
}
