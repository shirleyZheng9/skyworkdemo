package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.MessageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测目标输入数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测目标输入数据传输对象")
public class EvalTargetInputDataDTO {

  @Schema(description = "历史消息列表")
  private List<MessageDTO> historyMessages;

  @Schema(description = "输入字段")
  private Map<String, ContentDTO> inputFields;

  @Schema(description = "扩展字段")
  private Map<String, String> ext;
}
