package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 账号入参
 *
 * @author chen.linfa
 * @since 2025-08-05
 */
@Getter
@Setter
@ToString
@Schema(description = "账号入参")
public class AccountDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "用户名")
  private String username;
  @Schema(description = "密码")
  @JsonAlias("password")
  private String token;
  @Schema(description = "API Key")
  private String apiKey;
}
