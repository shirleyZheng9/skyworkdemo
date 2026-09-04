package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 账号配置 DTO（对应 bt_tenant_setting_info.setting_info 的 JSON）
 *
 * @author huangyunming
 * @since 2026-04-01
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeKnoraAccountSettingDTO {

  @Schema(description = "租户 ID")
  private Long tenantId;

  @JsonProperty("userName")
  private String userName;

  @JsonProperty("password")
  private String password;

  @JsonProperty("email")
  private String email;
}
