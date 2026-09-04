package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 过滤条件
 * 对应Go: FilterCondition
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterCondition {

  /**
   * 条件ID
   * 对应Go: ConditionID int64
   */
  private Long conditionId;

  /**
   * 字段名称
   * 对应Go: FieldName string
   */
  private String fieldName;

  /**
   * 操作符
   * 对应Go: Operator string
   */
  private String operator;

  /**
   * 值
   * 对应Go: Value interface{}
   */
  private Object value;

  /**
   * 值列表
   * 对应Go: Values []interface{}
   */
  private List<Object> values;

  /**
   * 逻辑操作符
   * 对应Go: LogicOperator string
   */
  private String logicOperator;

  /**
   * 子条件列表
   * 对应Go: SubConditions []*FilterCondition
   */
  private List<FilterCondition> subConditions;

  /**
   * 扩展配置
   * 对应Go: ExtConfig map[string]interface{}
   */
  private Map<String, Object> extConfig;
}
