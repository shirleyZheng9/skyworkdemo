package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ArgsSchemaDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测目标内容数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测目标内容数据传输对象")
public class EvalTargetContentDTO {

  @Schema(description = "输入模式列表")
  private List<ArgsSchemaDTO> inputSchemas;

  @Schema(description = "输出模式列表")
  private List<ArgsSchemaDTO> outputSchemas;

  @Schema(description = "Bot")
  private BotDTO bot;

  @Schema(description = "提示词")
  private EvalPromptDTO prompt;

  @Schema(description = "工作流")
  private WorkflowDTO workflow;
}
