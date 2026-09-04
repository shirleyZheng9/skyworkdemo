package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.FieldSchemaDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评测集字段请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新评测集Schema请求")
public class UpdateEvaluationSetSchemaRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集ID")
  private Long evaluationSetId;

  @Schema(description = "字段列表")
  private List<FieldSchemaDTO> fields;

  @Schema(description = "基础信息")
  private Base base;
}
