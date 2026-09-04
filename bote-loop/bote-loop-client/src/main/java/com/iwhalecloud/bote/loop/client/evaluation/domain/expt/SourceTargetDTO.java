package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 来源目标数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "来源目标数据传输对象")
public class SourceTargetDTO {

  @Schema(description = "评测目标类型")
  private EvalTargetTypeDTO evalTargetType;

  @Schema(description = "来源目标ID列表")
  private List<String> sourceTargetIds;
}
