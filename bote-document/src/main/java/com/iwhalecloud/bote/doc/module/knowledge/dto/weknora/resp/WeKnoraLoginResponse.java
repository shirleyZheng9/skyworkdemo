package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 登录响应
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Getter
@Setter
public class WeKnoraLoginResponse {

  @JsonProperty("success")
  private Boolean success;

  @JsonProperty("message")
  private String message;

  @JsonProperty("user")
  private UserInfo user;

  @JsonProperty("tenant")
  private TenantInfo tenant;

  @JsonProperty("token")
  private String token;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @Getter
  @Setter
  public static class UserInfo {
    @JsonProperty("id")
    private String id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("email")
    private String email;
    @JsonProperty("tenant_id")
    private Long tenantId;
    @JsonProperty("is_active")
    private Boolean isActive;
  }

  @Getter
  @Setter
  public static class TenantInfo {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    /** 租户 API Key（格式：sk-xxx），后续所有业务 API 鉴权使用 */
    @JsonProperty("api_key")
    private String apiKey;
    @JsonProperty("status")
    private String status;
  }
}
