package com.iwhalecloud.bote.common.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 百应内容类型
 *
 * @author bianjp
 * @since 2025-07-19
 */
@Getter
@RequiredArgsConstructor
public enum BeyondContentType {
  /** ui-agent 的表单 */
  @JsonProperty("2010")
  FORM("2010"),
  /** 思考内容 */
  @JsonProperty("1001")
  REASONING("1001"),
  /** 文本卡片 */
  @JsonProperty("1002")
  TEXT("1002"),
  /** 图表卡片 */
  @JsonProperty("2001")
  CHART("2001"),
  /** 博特卡片 */
  @JsonProperty("2011")
  BOTE("2011"),
  /** 博特页面函数 */
  @JsonProperty("2014")
  BOTE_PAGE_FUNC("2014"),;

  /** 内容类型编码 */
  private final String code;
}
