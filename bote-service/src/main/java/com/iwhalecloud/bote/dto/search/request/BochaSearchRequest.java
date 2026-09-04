package com.iwhalecloud.bote.dto.search.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * bocha web search 入参
 *
 * @author chen.linfa
 * @since 2025-03-13
 */
@Getter
@Setter
@ToString
public class BochaSearchRequest {
  /** 搜索内容 */
  private String query;
  /** 新鲜度 */
  private String freshness;
  /** 是否总结内容 */
  private Boolean summary;
  /** 召回页面数量 */
  private Integer count;
}
