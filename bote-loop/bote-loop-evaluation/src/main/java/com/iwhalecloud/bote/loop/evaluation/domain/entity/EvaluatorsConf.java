package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器配置实体
 * 迁移对应关系: Go语言EvaluatorsConf
 * - 功能: 评估器配置数据结构
 * - 字段: evaluatorConcurNum, evaluatorConf
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluatorsConf结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现Valid、GetEvaluatorConf、GetEvaluatorConcurNum方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *int -> Java Integer
 * - Go []*EvaluatorConf -> Java List<EvaluatorConf>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorsConf {
  @JsonProperty("evaluator_concur_num")
  private Integer evaluatorConcurNum;

  @JsonProperty("evaluator_conf")
  private List<EvaluatorConf> evaluatorConf;

  /**
   * 验证配置
   * 迁移对应关系: Go语言EvaluatorsConf.Valid()
   */
  public boolean valid() {
    if (this == null || this.evaluatorConf == null || this.evaluatorConf.isEmpty()) {
      return false;
    }
    for (EvaluatorConf conf : this.evaluatorConf) {
      if (!conf.valid()) {
        return false;
      }
    }
    return true;
  }

  /**
   * 获取评估器配置
   * 迁移对应关系: Go语言EvaluatorsConf.GetEvaluatorConf()
   */
  public EvaluatorConf getEvaluatorConf(Long evalVerId) {
    if (this.evaluatorConf == null) {
      return null;
    }
    for (EvaluatorConf conf : this.evaluatorConf) {
      if (conf.getEvaluatorVersionId().equals(evalVerId)) {
        return conf;
      }
    }
    return null;
  }

  /**
   * 获取评估器并发数
   * 迁移对应关系: Go语言EvaluatorsConf.GetEvaluatorConcurNum()
   */
  public int getEvaluatorConcurNum() {
    final int defaultConcurNum = 3;
    if (this.evaluatorConcurNum != null && this.evaluatorConcurNum > 0) {
      return this.evaluatorConcurNum;
    }
    return defaultConcurNum;
  }
}
