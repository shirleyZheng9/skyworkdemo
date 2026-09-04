package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.BotInfoTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评测目标参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建评测目标参数")
public class CreateEvalTargetParamDTO {

  @Schema(description = "源目标ID")
  private String sourceTargetId;

  @Schema(description = "源目标版本")
  private String sourceTargetVersion;

  @Schema(description = "评测目标类型")
  private EvalTargetTypeDTO evalTargetType;

  @Schema(description = "Bot信息类型")
  private BotInfoTypeDTO botInfoType;

  @Schema(description = "Bot发布版本")
  private String botPublishVersion;
}
