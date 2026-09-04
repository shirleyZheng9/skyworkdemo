package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测目标输出数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测目标输出数据传输对象")
public class EvalTargetOutputDataDTO {

  @Schema(description = "输出字段")
  private Map<String, ContentDTO> outputFields;

  @Schema(description = "评测目标使用情况")
  private EvalTargetUsageDTO evalTargetUsage;

  @Schema(description = "评测目标运行错误")
  private EvalTargetRunErrorDTO evalTargetRunError;

  @Schema(description = "耗时（毫秒）")
  private Long timeConsumingMs;
}
