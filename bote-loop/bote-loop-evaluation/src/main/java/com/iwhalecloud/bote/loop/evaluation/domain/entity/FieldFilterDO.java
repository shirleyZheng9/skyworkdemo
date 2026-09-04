package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段过滤器实体
 * 对应Go: entity.FieldFilter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldFilterDO {

  /**
   * 字段键
   * 对应Go: Key string
   */
  private String key;

  /**
   * 操作符
   * 对应Go: Op string // =, >, >=, <, <=, BETWEEN, LIKE
   */
  private String op;

  /**
   * 值列表
   * 对应Go: Values []any
   */
  private List<Object> values;
}
