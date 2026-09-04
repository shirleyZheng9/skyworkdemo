package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器配置实体
 * 迁移对应关系: Go语言EvaluatorConf
 * - 功能: 评估器配置数据结构
 * - 字段: evaluatorVersionId, ingressConf
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluatorConf结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现Valid方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go *EvaluatorIngressConf -> Java EvaluatorIngressConf
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorConf {
  @JsonProperty("evaluator_version_id")
  private Long evaluatorVersionId;

  @JsonProperty("ingress_conf")
  private EvaluatorIngressConf ingressConf;

  /**
   * 验证配置
   * 迁移对应关系: Go语言EvaluatorConf.Valid()
   */
  public boolean valid() {
    if (this == null || this.evaluatorVersionId == null || this.evaluatorVersionId == 0
      || this.ingressConf == null || this.ingressConf.getTargetAdapter() == null
      || this.ingressConf.getEvalSetAdapter() == null) {
      return false;
    }
    return true;
  }
}
