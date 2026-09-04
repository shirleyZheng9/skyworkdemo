package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建实验参数
 * 对应Go: CreateExptParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateExptParam {

  private Long workspaceId;
  private Long evalSetVersionId;
  private Long targetVersionId;
  private List<Long> evaluatorVersionIds;
  private String name;
  private String desc;
  private Long evalSetId;
  private Long targetId;
  private CreateEvalTargetParam createEvalTargetParam;
  private ExptType exptType;
  private Long maxAliveTime;
  private SourceType sourceType;
  private String sourceId;
  private EvaluationConfiguration exptConf;
  private Long catalogItemId;
}
