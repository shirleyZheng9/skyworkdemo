package com.iwhalecloud.bote.dto.lcdp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 灵犀平台应用版本信息 DTO
 *
 * @author qian.sisheng
 * @since 2025-06-04
 */

@Setter
@Getter
@ToString
@Schema(description = "灵犀平台应用版本信息")
public class LcdpAppVersionDTO {
  @Schema(description = "应用版本ID")
  private Long appVersionId;
  @Schema(description = "应用ID")
  private Long appId;
  @Schema(description = "应用名称")
  private String appName;
}
