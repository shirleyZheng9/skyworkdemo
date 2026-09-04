package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "实验数据传输对象")
public class ExperimentDTO implements UserInfoCarrier {
  @Schema(description = "实验ID")
  private Long id;
  @Schema(description = "实验名称")
  private String name;
  @Schema(description = "实验描述")
  private String desc;
  @Schema(description = "创建者ID")
  private String creatorBy;
  @Schema(description = "创建者名称")
  private String creatorByName;
  @Schema(description = "实验状态")
  private ExptStatusDTO status;
  @Schema(description = "状态消息")
  private String statusMessage;
  @Schema(description = "开始时间")
  private Long startTime;
  @Schema(description = "结束时间")
  private Long endTime;
  @Schema(description = "评测集版本ID")
  private Long evalSetVersionId;
  @Schema(description = "目标版本ID")
  private Long targetVersionId;
  @Schema(description = "评测器版本ID列表")
  private List<Long> evaluatorVersionIds;
  @Schema(description = "评测集信息")
  private EvaluationSetDTO evalSet;
  @Schema(description = "评测目标信息")
  private EvalTargetDTO evalTarget;
  @Schema(description = "评测器列表")
  private List<EvaluatorDTO> evaluators;
  @Schema(description = "评测集ID")
  private Long evalSetId;
  @Schema(description = "目标ID")
  private Long targetId;
  @Schema(description = "基础信息")
  private BaseInfoDTO baseInfo;
  @Schema(description = "实验统计信息")
  private ExptStatisticsDTO exptStats;
  @Schema(description = "目标字段映射")
  private TargetFieldMappingDTO targetFieldMapping;
  @Schema(description = "评测器字段映射列表")
  private List<EvaluatorFieldMappingDTO> evaluatorFieldMapping;
  @Schema(description = "实验类型")
  private ExptTypeDTO exptType;
  @Schema(description = "最大存活时间")
  private Long maxAliveTime;
  @Schema(description = "来源类型")
  private SourceTypeDTO sourceType;
  @Schema(description = "来源ID")
  private String sourceId;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
}
