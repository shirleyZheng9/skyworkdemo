package com.iwhalecloud.bote.dto.lcdp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 灵犀平台编排服务 DTO
 *
 * @author qian.sisheng
 * @since 2025-06-05
 */
@Getter
@Setter
@ToString
@Schema(description = "灵犀平台编排服务")
public class LcdpStandardServiceDTO {
  @Schema(description = "服务ID")
  private Long serviceId;
  @Schema(description = "服务名称")
  private String serviceName;
  @Schema(description = "服务编码")
  private String serviceCode;
  @Schema(description = "服务描述")
  private String serviceDesc;
  @Schema(description = "服务请求JSON")
  private String requestJson;
  @Schema(description = "服务响应JSON")
  private String responseJson;
  @Schema(description = "默认服务版本ID")
  private Long defaultVersionId;
  @Schema(description = "应用ID")
  private Long appId;
  @Schema(description = "服务版本ID")
  private Long serviceVersionId;
}
