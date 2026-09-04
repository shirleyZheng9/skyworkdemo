package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作簿锁定信息模型
 *
 * @author Aiqing
 * @since 2025-09-26
 */
@Getter
@Setter
@ToString
@Schema(description = "工作簿锁定信息")
public class WorkbookLockInfoDTO {

  @Schema(description = "锁定用户ID")
  private Long userId;

  @Schema(description = "锁定会话ID")
  private String sessionId;

  @Schema(description = "锁定时间戳")
  private Long lockTime;

  @Schema(description = "过期时间戳")
  private Long expireTime;

  @Schema(description = "锁定用户名称（缓存用，可选）")
  private String userName;

  /**
   * 构造函数
   */
  public WorkbookLockInfoDTO() {
  }

  /**
   * 构造函数
   *
   * @param userId 用户ID
   * @param sessionId 会话ID
   * @param lockTime 锁定时间戳
   * @param expireTime 过期时间戳
   */
  public WorkbookLockInfoDTO(Long userId, String sessionId, Long lockTime, Long expireTime) {
    this.userId = userId;
    this.sessionId = sessionId;
    this.lockTime = lockTime;
    this.expireTime = expireTime;
  }

  /**
   * 构造函数
   *
   * @param userId 用户ID
   * @param sessionId 会话ID
   * @param durationMinutes 锁定时长（分钟）
   */
  public WorkbookLockInfoDTO(Long userId, String sessionId, Integer durationMinutes) {
    this.userId = userId;
    this.sessionId = sessionId;
    this.lockTime = System.currentTimeMillis();
    this.expireTime = this.lockTime + (durationMinutes * 60 * 1000L);
  }

  /**
   * 获取锁定时间的Date对象
   *
   * @return 锁定时间
   */
  public Date getLockTimeAsDate() {
    return lockTime != null ? new Date(lockTime) : null;
  }

  /**
   * 获取过期时间的Date对象
   *
   * @return 过期时间
   */
  public Date getExpireTimeAsDate() {
    return expireTime != null ? new Date(expireTime) : null;
  }

  /**
   * 检查锁定是否已过期
   *
   * @return true-已过期，false-未过期
   */
  public boolean isExpired() {
    return expireTime != null && System.currentTimeMillis() > expireTime;
  }

  /**
   * 检查是否为指定用户锁定
   *
   * @param checkUserId 要检查的用户ID
   * @return true-是该用户锁定，false-不是
   */
  public boolean isLockedBy(Long checkUserId) {
    return userId != null && userId.equals(checkUserId);
  }

  /**
   * 检查是否为指定会话锁定
   *
   * @param checkSessionId 要检查的会话ID
   * @return true-是该会话锁定，false-不是
   */
  public boolean isLockedBySession(String checkSessionId) {
    return sessionId != null && sessionId.equals(checkSessionId);
  }

  /**
   * 检查是否为指定用户和会话锁定
   *
   * @param checkUserId 要检查的用户ID
   * @param checkSessionId 要检查的会话ID
   * @return true-是该用户和会话锁定，false-不是
   */
  public boolean isLockedByUserSession(Long checkUserId, String checkSessionId) {
    return isLockedBy(checkUserId) && isLockedBySession(checkSessionId);
  }
}
