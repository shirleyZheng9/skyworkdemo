package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 业务分类实体枚举
 * 对应Go: entity.BizCategory (string类型)
 */
public enum BizCategory {

  /**
   * 来自在线追踪
   * 对应Go: BizCategoryFromOnlineTrace = "from_online_trace"
   */
  FROM_ONLINE_TRACE("from_online_trace");

  private final String value;

  BizCategory(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static BizCategory fromString(String str) {
    for (BizCategory category : values()) {
      if (category.value.equals(str)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Invalid BizCategory string: " + str);
  }
}
