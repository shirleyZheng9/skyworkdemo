package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验元组ID实体
 * 迁移对应关系: Go语言ExptTupleID
 * - 功能: 实验元组ID数据结构
 * - 字段: versionedTargetID, versionedEvalSetID, evaluatorVersionIDs
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTupleID结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *VersionedTargetID -> Java VersionedTargetID
 * - Go *VersionedEvalSetID -> Java VersionedEvalSetID
 * - Go []int64 -> Java List<Long>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTupleID {
  @JsonProperty("versioned_target_id")
  private VersionedTargetID versionedTargetId;

  @JsonProperty("versioned_eval_set_id")
  private VersionedEvalSetID versionedEvalSetId;

  @JsonProperty("evaluator_version_ids")
  private List<Long> evaluatorVersionIds;
}
