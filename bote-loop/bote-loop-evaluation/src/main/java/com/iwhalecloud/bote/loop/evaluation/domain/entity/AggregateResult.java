package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聚合结果实体
 * 迁移对应关系: Go语言AggregateResult
 * - 功能: 聚合结果数据结构
 * - 字段: aggregatorResults
 * <p>
 * Java实现说明:
 * - 对应Go的AggregateResult结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []*AggregatorResult -> Java List<AggregatorResult>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregateResult {
  @JsonProperty("aggregator_results")
  private List<AggregatorResult> aggregatorResults;
}
