package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 过滤器数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "过滤器数据传输对象")
public class FiltersDTO {

  @Schema(description = "过滤条件列表")
  private List<FilterConditionDTO> filterConditions;

  @Schema(description = "逻辑操作符")
  private FilterLogicOpDTO logicOp;
}
