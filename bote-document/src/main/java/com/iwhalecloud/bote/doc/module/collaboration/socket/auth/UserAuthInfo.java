package com.iwhalecloud.bote.doc.module.collaboration.socket.auth;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用户认证信息
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class UserAuthInfo {

  /**
   * 用户ID
   */
  private Long userId;

  /**
   * 用户名
   */
  private String userCode;

  /**
   * 用户显示名称
   */
  private String userName;

  /**
   * 租户ID
   */
  private String tenantId;

  /**
   * token过期时间
   */
  private LocalDateTime expireTime;

  /**
   * token签发时间
   */
  private LocalDateTime issuedTime;

  /**
   * 客户端IP
   */
  private String clientIp;

  /**
   * 用户代理
   */
  private String userAgent;

  /**
   * 扩展属性
   */
  private Map<String, Object> attributes;

  public UserAuthInfo() {
    this.attributes = new ConcurrentHashMap<>();
  }

  /**
   * 设置扩展属性
   */
  public UserAuthInfo setAttribute(String key, Object value) {
    this.attributes.put(key, value);
    return this;
  }

  /**
   * 获取扩展属性
   */
  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String key) {
    return (T) this.attributes.get(key);
  }

  /**
   * 检查token是否过期
   */
  public boolean isExpired() {
    return expireTime != null && LocalDateTime.now().isAfter(expireTime);
  }
}
