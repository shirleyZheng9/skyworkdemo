package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 字段类型映射枚举
 * 迁移对应关系: Go语言entity.FieldTypeMapping
 * - 功能: 定义ExptTurnResultFilterKeyMapping中FieldType的常量
 * - 用途: 过滤键映射类型标识
 * <p>
 * Java实现说明:
 * - 对应Go的FieldTypeMapping枚举
 * - 提供值转换方法
 * - 支持类型判断
 * <p>
 * 技术栈迁移:
 * - Go type FieldTypeMapping int32 -> Java enum FieldTypeMapping
 * - Go int32值 -> Java Integer值
 */
public enum FieldTypeMapping {

  /**
   * 未知类型
   * 迁移对应关系: Go语言FieldTypeUnknown
   * - 值: 0
   * - 功能: 未知的映射类型
   */
  UNKNOWN(0),

  /**
   * 评估器类型
   * 迁移对应关系: Go语言FieldTypeEvaluator
   * - 值: 1
   * - 功能: 评估器相关的映射类型
   */
  EVALUATOR(1),

  /**
   * 人工标注类型
   * 迁移对应关系: Go语言FieldTypeManualAnnotation
   * - 值: 2
   * - 功能: 人工标注相关的映射类型
   */
  MANUAL_ANNOTATION(2);

  private final Integer value;

  FieldTypeMapping(Integer value) {
    this.value = value;
  }

  /**
   * 获取枚举值
   * 迁移对应关系: Go语言FieldTypeMapping的值
   *
   * @return 枚举值
   */
  public Integer getValue() {
    return value;
  }

  /**
   * 根据值获取枚举
   * 迁移对应关系: Go语言FieldTypeMapping的值转换
   *
   * @param value 枚举值
   * @return 对应的枚举
   */
  public static FieldTypeMapping fromValue(Integer value) {
    if (value == null) {
      return null;
    }
    for (FieldTypeMapping type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("未知的FieldTypeMapping值: " + value);
  }
}
