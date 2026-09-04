package com.iwhalecloud.bote.common.enums;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * 查询条件匹配类型
 *
 * @author chen.linfa
 * @since 2025-11-25
 */
@Getter
@RequiredArgsConstructor
public enum ParamsMatchType {
  /** 为空 */
  IS_NULL("isNull"),
  /** 不为空 */
  NOT_NULL("notNull"),
  /** 相等 */
  EQUALS("="),
  /** 不等 */
  NOT_EQUALS("!="),
  /** 大于 */
  GREAT_THAN(">"),
  /** 大于等于 */
  GREAT_THAN_OR_EQUALS(">="),
  /** 小于 */
  LESS_THAN("<"),
  /** 小于等于 */
  LESS_THAN_OR_EQUALS("<="),
  /** 在 */
  IN("in"),
  /** 不在 */
  NOT_IN("notIn"),
  /** 在范围内(包含两端) */
  BETWEEN("between"),
  /** 不在范围内 */
  NOT_BETWEEN("notBetween"),
  /** 包含字符串 */
  CONTAINS("contains"),
  /** 不包含字符串 */
  NOT_CONTAINS("notContains"),
  /** 以字符串开始 */
  STARTS_WITH("startsWith"),
  /** 以字符串结尾 */
  ENDS_WITH("endsWith");

  /** 编码 */
  private final String code;

  /**
   * 根据编码获取匹配类型
   */
  public static ParamsMatchType findMatchType(@Nullable String code) {
    // 默认使用相等
    if (code == null || code.isEmpty()) {
      return EQUALS;
    }
    for (ParamsMatchType value : values()) {
      if (code.equals(value.getCode())) {
        return value;
      }
    }
    throw new BssException("不支持的查询条件匹配类型: " + code);
  }

  /**
   * 是否是算术比较
   */
  public boolean isArithmeticCompare() {
    return this == EQUALS || this == NOT_EQUALS || this == GREAT_THAN || this == GREAT_THAN_OR_EQUALS || this == LESS_THAN ||
           this == LESS_THAN_OR_EQUALS;
  }

  /**
   * 是否是模糊匹配
   */
  public boolean isFuzzyMatching() {
    return this == CONTAINS || this == NOT_CONTAINS || this == STARTS_WITH || this == ENDS_WITH;
  }

  /**
   * 是否是范围
   */
  public boolean isBetween() {
    return this == BETWEEN || this == NOT_BETWEEN;
  }

  /**
   * 是否是范围
   */
  public boolean isNull() {
    return this == IS_NULL || this == NOT_NULL;
  }

  /**
   * 获取参数数量
   *
   * @return 参数数量
   */
  public int getParamCount() {
    if (isNull()) {
      return 0;
    }
    else if (isBetween()) {
      return 2;
    }
    return 1;
  }
}
