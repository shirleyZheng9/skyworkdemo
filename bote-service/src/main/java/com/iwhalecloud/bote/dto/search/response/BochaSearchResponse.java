package com.iwhalecloud.bote.dto.search.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

import com.iwhalecloud.bote.dto.search.WebPageInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * bocha web search 出参
 *
 * @author chen.linfa
 * @since 2025-03-13
 */
@Getter
@Setter
@ToString
public class BochaSearchResponse {
  /** 状态码，200 表示成功, 其它均为失败 */
  private String code;
  /** 提示信息 */
  private String msg;
  /** 返回对象 */
  private SearchInfo data;

  @JsonIgnore
  public boolean isSuccess() {
    return "200".equals(this.code);
  }

  /**
   * 搜索结果
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SearchInfo {
    private WebPage webPages;
  }

  /**
   * 网页信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WebPage {
    /** 页面搜索链接 */
    private String webSearchUrl;
    /** 页面定义 */
    private List<WebPageInfo> value;
  }

  /**
   * 分组网页定义
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WebPageInfoGroup {
    /** 搜索内容的标题 */
    private String title;
    /** 网页 */
    private List<WebPageInfo> list;
  }
}
