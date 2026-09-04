package com.iwhalecloud.bote.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth2令牌响应
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString
@Schema(description = "OAuth2令牌响应")
public class OAuth2TokenResponse {

  @JsonProperty("access_token")
  @Schema(description = "访问令牌")
  private String accessToken;

  @JsonProperty("token_type")
  @Schema(description = "令牌类型")
  private String tokenType = "Bearer";

  @JsonProperty("expires_in")
  @Schema(description = "过期时间(秒)")
  private Integer expiresIn;

  @JsonProperty("refresh_token")
  @Schema(description = "刷新令牌")
  private String refreshToken;

  @JsonProperty("scope")
  @Schema(description = "权限范围")
  private String scope;

  @JsonProperty("id_token")
  @Schema(description = "ID令牌")
  private String idToken;
}
