package com.iwhalecloud.bote.dto.tenant.setting;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户的 DocChain 账号设置
 *
 * @author bianjp
 * @since 2025-01-16
 */
@Getter
@Setter
@ToString
public class TenantDocChainAccountSettingDTO {
  private String userName;
  /** 不要使用该字段，为割接反序列化保留，请使用token  */
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;
  private String token;
  private String apiKey;
  private Boolean enabled;
}
