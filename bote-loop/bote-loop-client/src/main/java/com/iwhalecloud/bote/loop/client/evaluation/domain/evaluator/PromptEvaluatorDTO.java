package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.MessageDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ModelConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提示评测器数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "提示评测器数据传输对象")
public class PromptEvaluatorDTO {

  @Schema(description = "消息列表")
  private List<MessageDTO> messageList;

  @Schema(description = "模型配置")
  private ModelConfigDTO modelConfig;

  @Schema(description = "提示源类型")
  private PromptSourceTypeDTO promptSourceType;

  @Schema(description = "提示模板键")
  private String promptTemplateKey;

  @Schema(description = "提示模板名称")
  private String promptTemplateName;

  @Schema(description = "工具列表")
  private List<ToolDTO> tools;

  @Schema(description = "通过得分")
  private Double passScore;
}
