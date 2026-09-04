package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据项快照过滤器实体
 * 对应Go: entity.ItemSnapshotFilter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSnapshotFilterDO {

  /**
   * 布尔值Map过滤器列表
   * 对应Go: BoolMapFilters []*FieldFilter
   */
  private List<FieldFilterDO> boolMapFilters;

  /**
   * 浮点数Map过滤器列表
   * 对应Go: FloatMapFilters []*FieldFilter
   */
  private List<FieldFilterDO> floatMapFilters;

  /**
   * 整数Map过滤器列表
   * 对应Go: IntMapFilters []*FieldFilter
   */
  private List<FieldFilterDO> intMapFilters;

  /**
   * 字符串Map过滤器列表
   * 对应Go: StringMapFilters []*FieldFilter
   */
  private List<FieldFilterDO> stringMapFilters;
}
