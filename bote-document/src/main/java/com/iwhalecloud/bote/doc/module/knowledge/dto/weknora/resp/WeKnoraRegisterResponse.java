package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 注册响应
 *
 * @author huangyunming
 * @since 2026-04-01
 */
@Getter
@Setter
public class WeKnoraRegisterResponse {

  @JsonProperty("success")
  private Boolean success;

  @JsonProperty("message")
  private String message;

  @JsonProperty("user")
  private UserInfo user;

  @Getter
  @Setter
  public static class UserInfo {
    @JsonProperty("id")
    private String id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("email")
    private String email;
    @JsonProperty("avatar")
    private String avatar;
    @JsonProperty("tenant_id")
    private Long tenantId;
    @JsonProperty("is_active")
    private Boolean isActive;
    @JsonProperty("can_access_all_tenants")
    private Boolean canAccessAllTenants;
  }
}
