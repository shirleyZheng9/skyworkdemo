package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * API 鉴权信息
 *
 * @author bianjp
 * @since 2025-04-08
 */
@Getter
@Setter
@ToString
public class SimpleApiAuthDTO {
  /** 令牌失效时间 */
  private Date expTime;
  /** 租户 ID */
  private Long tenantId;
  /** 用户 ID (作为登录信息） */
  protected Long userId;
  /** 用户名 */
  private String userName;
  /** 用户姓名 */
  private String realName;

  /**
   * 检查是否已过期
   */
  @JsonIgnore
  public boolean isExpired() {
    return expTime == null || expTime.getTime() <= System.currentTimeMillis();
  }

  /**
   * 构造登录信息
   */
  public LoginInfo toLoginInfo() {
    return LoginInfo.builder()
      .userId(userId)
      .userName(userName)
      .realName(realName)
      .defaultTenantId(tenantId)
      .build();
  }
}
