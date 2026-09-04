package com.iwhalecloud.bote.dto.plugin.lark;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.time.DateUtils;
import java.util.Date;

/**
 * 飞书 userAccessToken 信息
 *
 * @author qian.sisheng
 * @since 2025-08-20
 */
@Setter
@Getter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class LarkUserAccessTokenDTO {
  /** 错误码 0表示成功 */
  private Integer code;
  /** 错误类型，仅在请求失败时返回 */
  private String error;
  /** 错误描述，仅在请求失败时返回 */
  private String errorDescription;
  /** user_access_token */
  private String accessToken;
  /** user_access_token 的有效期，单位为秒 */
  private Integer expiresIn;
  /** refresh_token, 用于刷新 user_access_token */
  private String refreshToken;
  /** refresh_token 的有效期，单位为秒 */
  private Integer refreshTokenExpiresIn;
  /** 用户信息 */
  private LarkUserAuthInfoDTO userInfo;
  /** access_token 获取时间 */
  private Date accessTokenObtainedTime;
  /** refresh_token 获取时间 */
  private Date refreshTokenObtainedTime;
  /** access_token 过期时间 */
  private Date accessTokenExpireTime;
  /** refresh_token 过期时间 */
  private Date refreshTokenExpireTime;
  /** 权限范围 */
  private String scope;

  /**
   * 在设置 expireIn 时自动计算过期时间
   */
  public void setExpiresIn(Integer expiresIn) {
    this.expiresIn = expiresIn;
    if (expiresIn != null && expiresIn > 0) {
      this.accessTokenObtainedTime = new Date();
      this.accessTokenExpireTime = DateUtils.addSeconds(accessTokenObtainedTime, expiresIn);
    }
  }

  /**
   * 检查 access_token 是否过期
   *
   * @return true 表示已过期，false 表示未过期
   */
  public boolean isAccessTokenExpired() {
    if (accessTokenExpireTime == null) {
      return true;
    }
    // 提前5分钟认为过期，避免临界问题
    Date expireThreshold = DateUtils.addMinutes(accessTokenExpireTime, -5);
    return new Date().after(expireThreshold);
  }

  /**
   * 设置 refreshTokenExpireIn 时记录获取时间
   */
  public void setRefreshTokenExpiresIn(Integer refreshTokenExpiresIn) {
    this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    if (refreshTokenExpiresIn != null && refreshTokenExpiresIn > 0) {
      this.refreshTokenObtainedTime = new Date();
      this.refreshTokenExpireTime = DateUtils.addSeconds(refreshTokenObtainedTime, refreshTokenExpiresIn);
    }
  }

  /**
   * 检查 refresh_token 是否已过期
   *
   * @return true 表示已过期，false 表示未过期
   */
  public boolean isRefreshTokenExpired() {
    if (refreshTokenExpireTime == null) {
      return true;
    }
    // 提前5分钟认为过期，避免临界问题
    Date expireThreshold = DateUtils.addMinutes(refreshTokenExpireTime, -5);
    return new Date().after(expireThreshold);
  }
}
