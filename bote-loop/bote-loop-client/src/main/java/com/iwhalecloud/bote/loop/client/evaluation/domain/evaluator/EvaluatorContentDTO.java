package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ArgsSchemaDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测器内容数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测器内容数据传输对象")
public class EvaluatorContentDTO {

  @Schema(description = "是否接收聊天历史")
  private Boolean receiveChatHistory;

  @Schema(description = "输入Schema列表")
  private List<ArgsSchemaDTO> inputSchemas;

  @Schema(description = "提示词评测器")
  private PromptEvaluatorDTO promptEvaluator;

  @Schema(description = "代码评测器")
  private CodeEvaluatorDTO codeEvaluator;
}
