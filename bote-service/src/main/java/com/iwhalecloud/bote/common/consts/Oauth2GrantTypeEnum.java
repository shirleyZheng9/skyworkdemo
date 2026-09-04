package com.iwhalecloud.bote.common.consts;

import java.util.Arrays;
import lombok.Getter;

/**
 * oauth2的授权类型
 *
 * @author Aiqing
 * @since 2025/12/27
 */
@Getter
public enum Oauth2GrantTypeEnum {

  /**
   * 授权码
   */
  AUTHORIZATION_CODE("authorization_code"),
  /**
   * 刷新token
   */
  REFRESH_TOKEN("refresh_token"),

  /**
   * 客户端凭证
   */
  CLIENT_CREDENTIALS("client_credentials");

  private final String code;

  Oauth2GrantTypeEnum(String code) {
    this.code = code;
  }

  public static Oauth2GrantTypeEnum getByCode(String code) {
    if (code == null || code.trim().isEmpty()) {
      return null;
    }
    return Arrays.stream(values()).filter(item -> {
        return item.getCode().equalsIgnoreCase(code);
      }).findAny()
      .orElse(null);
  }
}
