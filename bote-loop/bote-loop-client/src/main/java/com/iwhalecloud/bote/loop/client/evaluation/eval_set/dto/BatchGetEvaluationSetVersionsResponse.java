package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 批量获取评估集版本响应
 * 对应Go: BatchGetEvaluationSetVersionsResponse
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取评测集版本响应")
public class BatchGetEvaluationSetVersionsResponse extends BaseResponse {

  @Schema(description = "版本化评估集列表")
  private List<VersionedEvaluationSetDTO> versionedEvaluationSets;
}
