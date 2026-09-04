package com.iwhalecloud.bote.doc.module.collaboration.socket.session;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 会话信息
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SessionInfo {

  /**
   * 会话ID
   */
  private String sessionId;

  /**
   * 租户ID
   */
  private Long tenantId;

  /**
   * 用户ID
   */
  private Long userId;

  /**
   * 终端类型，默认1
   */
  private Integer terminalType;

  /**
   * 连接时间
   */
  private LocalDateTime connectTime;

  /**
   * 最后活跃时间
   */
  private LocalDateTime lastActiveTime;

  /**
   * 扩展属性
   */
  private java.util.Map<String, Object> attributes;

  public SessionInfo() {
    this.connectTime = LocalDateTime.now();
    this.lastActiveTime = LocalDateTime.now();
    this.attributes = new java.util.concurrent.ConcurrentHashMap<>();
  }

  /**
   * 更新最后活跃时间
   */
  public void updateLastActiveTime() {
    this.lastActiveTime = LocalDateTime.now();
  }

  /**
   * 设置扩展属性
   */
  public void setAttribute(String key, Object value) {
    this.attributes.put(key, value);
  }

  /**
   * 获取扩展属性
   */
  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String key) {
    return (T) this.attributes.get(key);
  }
}
