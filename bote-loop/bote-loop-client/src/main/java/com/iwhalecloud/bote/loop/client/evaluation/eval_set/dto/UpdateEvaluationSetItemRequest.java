package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.TurnDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评估集数据项请求
 * 对应Go: UpdateEvaluationSetItemRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新评测集数据项请求")
public class UpdateEvaluationSetItemRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评估集ID")
  private Long evaluationSetId;

  @Schema(description = "数据项ID")
  private Long itemId;

  @Schema(description = "每轮对话")
  private List<TurnDTO> turns;

  @Schema(description = "基础信息")
  private Base base;
}
