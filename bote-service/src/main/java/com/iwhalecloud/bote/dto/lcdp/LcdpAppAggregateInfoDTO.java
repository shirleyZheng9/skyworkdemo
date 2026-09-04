package com.iwhalecloud.bote.dto.lcdp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 应用聚合信息 DTO
 *
 * @author qian.sisheng
 * @since 2025-06-05
 */
@Setter
@Getter
@ToString
public class LcdpAppAggregateInfoDTO {
  @Schema(description = "编排服务列表")
  private List<LcdpStandardServiceDTO> standardServiceList;
  @Schema(description = "静态数据列表")
  private List<LcdpAttrSpecDTO> appAttrSpecList;
  @Schema(description = "页面实例列表")
  private List<LcdpPageInstDTO> pageInstList;
}
