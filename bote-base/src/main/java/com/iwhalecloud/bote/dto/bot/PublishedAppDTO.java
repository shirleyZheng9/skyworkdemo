package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 已上架智能应用 DTO
 *
 * @author lizuyin
 * @since 2025-03-04
 */
@Getter
@Setter
@ToString
public class PublishedAppDTO {
  @Schema(description = "应用ID")
  private Long botId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "应用名称")
  private String appName;
  @Schema(description = "应用描述")
  private String appDesc;
}
