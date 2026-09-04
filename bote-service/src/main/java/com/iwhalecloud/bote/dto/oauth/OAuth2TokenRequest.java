package com.iwhalecloud.bote.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth2令牌请求
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString
@Schema(description = "OAuth2令牌请求")
public class OAuth2TokenRequest {

  @Schema(description = "授权类型")
  @JsonProperty("grant_type")
  private String grantType;

  @Schema(description = "授权码")
  private String code;

  @Schema(description = "重定向URI")
  @JsonProperty("redirect_uri")
  private String redirectUri;

  @Schema(description = "客户端ID")
  @JsonProperty("client_id")
  private String clientCode;

  @Schema(description = "客户端密钥")
  @JsonProperty("client_secret")
  private String clientSecret;

  @Schema(description = "刷新令牌")
  @JsonProperty("refresh_token")
  private String refreshToken;

  @Schema(description = "权限范围")
  private String scope;
}
