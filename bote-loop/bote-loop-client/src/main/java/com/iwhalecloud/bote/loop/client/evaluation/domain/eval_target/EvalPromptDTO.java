package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测提示词数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测提示词数据传输对象")
public class EvalPromptDTO {

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "版本")
  private String version;

  @Schema(description = "名称")
  private String name;

  @Schema(description = "提示词Key")
  private String promptKey;

  @Schema(description = "提交状态")
  private SubmitStatusDTO submitStatus;

  @Schema(description = "描述")
  private String description;
}
