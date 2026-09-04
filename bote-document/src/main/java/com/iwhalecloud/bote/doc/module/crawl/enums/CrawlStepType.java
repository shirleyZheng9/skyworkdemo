package com.iwhalecloud.bote.doc.module.crawl.enums;

import org.springframework.lang.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 爬取步骤基本定义
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
@Getter
@RequiredArgsConstructor
public enum CrawlStepType {

  /** 获取原始 HTML */
  FETCH(10, "获取原始数据"),
  /** 后处理 HTML */
  POSTPROCESS(20, "后处理"),
  /** 清理 HTML */
  CLEAN(30, "清理"),
  /** 转换 HTML */
  TRANSFORM(40, "转换"),
  /** 封装 HTML */
  WRAP(50, "封装");

  /** 类型值 */
  private final int value;

  /** 名称 */
  private final String name;

  /**
   * 根据类型值获取类型枚举值
   *
   * @param value 类型值
   * @return 类型枚举值
   */
  @Nullable
  public static CrawlStepType findByValue(@Nullable Integer value) {
    if (value == null) {
      return null;
    }
    for (CrawlStepType type : values()) {
      if (value.equals(type.getValue())) {
        return type;
      }
    }
    return null;
  }
}
