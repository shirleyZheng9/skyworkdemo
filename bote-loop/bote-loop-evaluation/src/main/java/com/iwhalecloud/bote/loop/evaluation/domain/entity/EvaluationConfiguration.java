package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估配置实体
 * 迁移对应关系: Go语言EvaluationConfiguration
 * - 功能: 评估配置数据结构
 * - 字段: connectorConf, itemConcurNum
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluationConfiguration结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go Connector -> Java Connector
 * - Go *int -> Java Integer
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationConfiguration {
  @JsonProperty("connector_conf")
  private Connector connectorConf;

  @JsonProperty("item_concur_num")
  private Integer itemConcurNum;
}
