package com.iwhalecloud.bote.service.base;

import org.springframework.lang.Nullable;

/**
 * 会话消息归档服务
 *
 * @author qian.sisheng
 * @since 2024-12-6
 */
public interface IArchiveMessageService {
  /**
   *
   * 归档会话
   */
  void archiveSessions();

  /**
   * 根据会话 ID 归档会话
   *
   * <p>删除单个会话时使用</p>
   *
   * @param sessionId 会话 ID
   */
  void archiveSession(Long sessionId);

  /**
   * 根据智能应用 ID 归档会话
   *
   * <p>清空所有会话、清空单个智能应用的会话时使用</p>
   *
   * @param spaceId 空间 ID
   * @param tenantId 租户 ID
   * @param botId 智能应用 ID, 用于筛选会话，可选
   * @param botTenantId 应用归属租户 ID
   * @param creatorId 用户 ID, 用于筛选会话的创建人
   */
  void archiveSessionsByTenantIdAndBotId(@Nullable Long spaceId, Long tenantId, @Nullable Long botId,  @Nullable Long botTenantId, Long creatorId);

  /**
   *
   * 清理历史会话
   */
  void clearHistorySessions();
}
