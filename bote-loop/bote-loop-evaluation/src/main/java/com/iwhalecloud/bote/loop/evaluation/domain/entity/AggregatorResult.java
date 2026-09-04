package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聚合器结果实体
 * 迁移对应关系: Go语言AggregatorResult
 * - 功能: 聚合器结果数据结构
 * - 字段: aggregatorType, data
 * <p>
 * Java实现说明:
 * - 对应Go的AggregatorResult结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现GetScore方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go AggregatorType -> Java AggregatorType
 * - Go *AggregateData -> Java AggregateData
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatorResult {
  @JsonProperty("aggregator_type")
  private AggregatorType aggregatorType;

  @JsonProperty("data")
  private AggregateData data;

  /**
   * 获取分数
   * 迁移对应关系: Go语言AggregatorResult.GetScore()
   */
  public double getScore() {
    if (this.data == null) {
      return 0.0;
    }
    if (this.data.getValue() == null) {
      return 0.0;
    }
    return this.data.getValue();
  }
}
