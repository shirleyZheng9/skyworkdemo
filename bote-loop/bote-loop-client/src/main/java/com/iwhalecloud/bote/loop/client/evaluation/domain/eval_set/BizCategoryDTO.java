package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 业务分类枚举
 */
public enum BizCategoryDTO {
  /** 来自在线追踪 */
  @JsonProperty("from_online_trace")
  FROM_ONLINE_TRACE("from_online_trace");

  private final String value;

  BizCategoryDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static BizCategoryDTO fromValue(String value) {
    for (BizCategoryDTO category : values()) {
      if (category.value.equals(value)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown biz category: " + value);
  }
}
