package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器入口配置实体
 * 迁移对应关系: Go语言EvaluatorIngressConf
 * - 功能: 评估器入口配置数据结构
 * - 字段: evalSetAdapter, targetAdapter, customConf
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluatorIngressConf结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *FieldAdapter -> Java FieldAdapter
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorIngressConf {
  @JsonProperty("eval_set_adapter")
  private FieldAdapter evalSetAdapter;

  @JsonProperty("target_adapter")
  private FieldAdapter targetAdapter;

  @JsonProperty("custom_conf")
  private FieldAdapter customConf;

  @JsonProperty("pass_score")
  private Double passScore;
}
