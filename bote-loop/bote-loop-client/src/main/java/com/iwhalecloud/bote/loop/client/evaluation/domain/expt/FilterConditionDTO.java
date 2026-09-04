package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 过滤条件数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "过滤条件数据传输对象")
public class FilterConditionDTO {

  @Schema(description = "过滤字段")
  private FilterFieldDTO field;

  @Schema(description = "操作符")
  private FilterOperatorTypeDTO operator;

  @Schema(description = "过滤值")
  private String value;

  @Schema(description = "来源目标")
  private SourceTargetDTO sourceTarget;
}
