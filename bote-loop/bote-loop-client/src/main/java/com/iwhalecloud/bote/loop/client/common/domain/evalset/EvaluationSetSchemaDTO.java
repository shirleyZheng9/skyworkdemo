package com.iwhalecloud.bote.loop.client.common.domain.evalset;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.FieldSchemaDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测集Schema数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationSetSchemaDTO {

  /**
   * 主键&外键
   */
  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("workspace_id")
  private Long workspaceId;

  @JsonProperty("evaluation_set_id")
  private Long evaluationSetId;

  /**
   * 数据集字段约束
   */
  @JsonProperty("field_schemas")
  private List<FieldSchemaDTO> fieldSchemas;

  /**
   * 系统信息
   */
  @JsonProperty("base_info")
  private BaseInfoDTO baseInfo;
}
