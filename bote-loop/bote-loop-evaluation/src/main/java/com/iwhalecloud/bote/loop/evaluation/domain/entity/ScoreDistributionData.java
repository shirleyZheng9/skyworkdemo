package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分数分布数据实体
 * 迁移对应关系: Go语言ScoreDistributionData
 * - 功能: 分数分布数据结构
 * - 字段: scoreDistributionItems
 * <p>
 * Java实现说明:
 * - 对应Go的ScoreDistributionData结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []*ScoreDistributionItem -> Java List<ScoreDistributionItem>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreDistributionData {
  @JsonProperty("score_distribution_items")
  private List<ScoreDistributionItem> scoreDistributionItems;
}
