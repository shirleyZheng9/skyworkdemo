package com.iwhalecloud.bote.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth2用户信息
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString
@Schema(description = "OAuth2用户信息")
public class OAuth2UserInfo {

  @Schema(description = "用户ID")
  private String sub;

  @Schema(description = "用户名")
  private String name;

  @Schema(description = "登录名")
  @JsonProperty("preferred_username")
  private String preferredUsername;

  @Schema(description = "邮箱")
  private String email;

  @Schema(description = "邮箱是否已验证")
  @JsonProperty("email_verified")
  private Boolean emailVerified;

  @Schema(description = "角色列表")
  private String[] roles;

  @Schema(description = "组列表")
  private String[] groups;

  @Schema(description = "租户ID")
  @JsonProperty("tenant_id")
  private String tenantId;

  @Schema(description = "租户名称")
  @JsonProperty("tenant_name")
  private String tenantName;
}
