package com.iwhalecloud.bote.dto.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网关信息简单 DTO（仅包含必要字段）
 *
 * @author lizuyin
 * @since 2026-01-21
 */
@Getter
@Setter
@ToString
public class PublishGatewaySimpleDTO {
  @Schema(description = "网关ID")
  private Long gatewayId;

  @Schema(description = "租户ID")
  private Long tenantId;

  @Schema(description = "网关URL")
  private String gatewayUrl;

  @Schema(description = "网关Token")
  private String gatewayToken;
}

