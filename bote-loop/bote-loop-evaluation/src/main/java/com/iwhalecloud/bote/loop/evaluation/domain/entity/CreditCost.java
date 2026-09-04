package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 信用成本枚举
 * 迁移对应关系: Go语言entity.CreditCost
 * - 功能: 定义实验的信用成本类型
 * - 用途: 信用成本管理和计费逻辑
 * <p>
 * Java实现说明:
 * - 对应Go的CreditCost枚举
 * - 提供值转换方法
 * - 支持成本类型判断
 * <p>
 * 技术栈迁移:
 * - Go type CreditCost int -> Java enum CreditCost
 * - Go int值 -> Java Integer值
 */
public enum CreditCost {

  /**
   * 默认成本
   * 迁移对应关系: Go语言CreditCostDefault
   * - 值: 0
   * - 功能: 默认的信用成本
   */
  DEFAULT(0),

  /**
   * 免费
   * 迁移对应关系: Go语言CreditCostFree
   * - 值: 1
   * - 功能: 免费的信用成本
   */
  FREE(1);

  private final Integer value;

  CreditCost(Integer value) {
    this.value = value;
  }

  /**
   * 获取枚举值
   * 迁移对应关系: Go语言CreditCost的值
   *
   * @return 枚举值
   */
  public Integer getValue() {
    return value;
  }

  /**
   * 根据值获取枚举
   * 迁移对应关系: Go语言CreditCost的值转换
   *
   * @param value 枚举值
   * @return 对应的枚举
   */
  public static CreditCost fromValue(Integer value) {
    if (value == null) {
      return null;
    }
    for (CreditCost cost : values()) {
      if (cost.value.equals(value)) {
        return cost;
      }
    }
    throw new IllegalArgumentException("未知的CreditCost值: " + value);
  }

  /**
   * 判断是否为免费
   *
   * @return 是否为免费
   */
  public boolean isFree() {
    return this == FREE;
  }

  /**
   * 判断是否为默认成本
   *
   * @return 是否为默认成本
   */
  public boolean isDefault() {
    return this == DEFAULT;
  }
}
