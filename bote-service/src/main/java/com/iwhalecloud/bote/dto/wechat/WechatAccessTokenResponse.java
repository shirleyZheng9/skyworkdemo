package com.iwhalecloud.bote.dto.wechat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * 微信获取access_token响应
 *
 * @author lizuyin
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString
public class WechatAccessTokenResponse {

  /** 获取到的凭证 */
  @JsonProperty("access_token")
  private String accessToken;
  /** 凭证有效时间，单位：秒 */
  @JsonProperty("expires_in")
  private Integer expiresIn;
  /** 错误码，0表示成功 */
  @JsonProperty("errcode")
  private Integer errCode;
  /** 错误信息 */
  @JsonProperty("errmsg")
  private String errMsg;

  /**
   * 判断是否成功获取access_token
   *
   * @return true表示成功，false表示失败
   */
  @JsonIgnore
  public boolean isSuccess() {
    return errCode == null || Objects.equals(0, errCode);
  }
}
