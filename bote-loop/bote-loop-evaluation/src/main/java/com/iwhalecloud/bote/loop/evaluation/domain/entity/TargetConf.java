package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 目标配置实体
 * 迁移对应关系: Go语言TargetConf
 * - 功能: 目标配置数据结构
 * - 字段: targetVersionId, ingressConf
 * <p>
 * Java实现说明:
 * - 对应Go的TargetConf结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现Valid方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go *TargetIngressConf -> Java TargetIngressConf
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetConf {
  @JsonProperty("target_version_id")
  private Long targetVersionId;

  @JsonProperty("ingress_conf")
  private TargetIngressConf ingressConf;

  /**
   * 验证配置
   * 迁移对应关系: Go语言TargetConf.Valid()
   */
  public boolean valid(EvalTargetType targetType) {
    if (this.targetVersionId != null && this.targetVersionId != 0 && this.ingressConf != null && this.ingressConf.getEvalSetAdapter() != null) {
      if (targetType == EvalTargetType.LOOP_PROMPT ||
        (this.ingressConf.getEvalSetAdapter().getFieldConfs() != null
          && !this.ingressConf.getEvalSetAdapter().getFieldConfs().isEmpty())) {
        return true;
      }
    }
    return false;
  }
}
