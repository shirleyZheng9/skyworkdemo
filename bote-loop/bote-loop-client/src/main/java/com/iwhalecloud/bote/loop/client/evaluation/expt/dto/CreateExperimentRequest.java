package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.EvaluatorFieldMappingDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.SourceTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TargetFieldMappingDTO;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.CreateEvalTargetParamDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建实验请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建实验请求")
public class CreateExperimentRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集版本ID")
  private Long evalSetVersionId;

  @Schema(description = "目标版本ID")
  private Long targetVersionId;

  @Schema(description = "评测器版本ID列表")
  private List<Long> evaluatorVersionIds;

  @Schema(description = "实验名称")
  private String name;

  @Schema(description = "实验描述")
  private String desc;

  @Schema(description = "评测集ID")
  private Long evalSetId;

  @Schema(description = "目标ID")
  private Long targetId;

  @Schema(description = "目标字段映射")
  private TargetFieldMappingDTO targetFieldMapping;

  @Schema(description = "评测器字段映射列表")
  private List<EvaluatorFieldMappingDTO> evaluatorFieldMapping;

  @Schema(description = "项目并发数")
  private Integer itemConcurNum;

  @Schema(description = "评测器并发数")
  private Integer evaluatorsConcurNum;

  @Schema(description = "创建评测目标参数")
  private CreateEvalTargetParamDTO createEvalTargetParam;

  @Schema(description = "实验类型")
  private ExptTypeDTO exptType;

  @Schema(description = "最大存活时间")
  private Long maxAliveTime;

  @Schema(description = "来源类型")
  private SourceTypeDTO sourceType;

  @Schema(description = "来源ID")
  private String sourceId;

  @Schema(description = "会话信息")
  private SessionDTO session;

  @Schema(description = "基础信息")
  private Base base;

  @Schema(description = "目录 ID")
  private Long catalogItemId;
}
