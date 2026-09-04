package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关键词映射条件实体
 * 迁移对应关系: Go语言KeywordMapCond
 * - 功能: 关键词映射条件
 * - 字段: itemSnapshotFilter, evalTargetDataFilters, keyword
 * <p>
 * Java实现说明:
 * - 对应Go的KeywordMapCond结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *ItemSnapshotFilter -> Java ItemSnapshotFilter
 * - Go []*FieldFilter -> Java List<FieldFilter>
 * - Go *string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordMapCond {
  @JsonProperty("item_snapshot_filter")
  private ItemSnapshotFilter itemSnapshotFilter;

  @JsonProperty("eval_target_data_filters")
  private List<FieldFilter> evalTargetDataFilters;

  @JsonProperty("keyword")
  private String keyword;
}
