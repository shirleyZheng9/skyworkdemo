package com.iwhalecloud.bote.dto.search.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.dto.search.WebPageInfo;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Tavily 搜索响应 DTO
 *
 * @author wangtingyun
 * @since 2026-03-30
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TavilySearchResponse {
  /** 查询内容 */
  private String query;
  /** 跟随答案 */
  private String answer;
  /** 图像列表 */
  private List<String> images;
  /** 搜索结果列表 */
  private List<SearchResult> results;
  /** 响应时间 */
  @JsonProperty("response_time")
  private Double responseTime;
  /** 错误详细信息 */
  private Map<String, String> detail;

  /**
   * 判断是否成功
   * @return true/false
   */
  @JsonIgnore
  public boolean isSuccess() {
    return this.query != null;
  }

  /**
   * 获取错误信息
   * @return 错误信息
   */
  @JsonIgnore
  public String getErrorMessage() {
    if (this.detail == null) {
      return null;
    }
    return this.detail.get("error");
  }

  /**
   * 单个搜索结果
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SearchResult {
    /** 标题 */
    private String title;
    /** 内容摘要 */
    private String content;
    /** URL 链接 */
    private String url;
    /** 分数 */
    private Double score;
    /** 原始内容 */
    @JsonProperty("raw_content")
    private String rawContent;
    /** 站点图标 */
    private String favicon;

    /**
     * 转换为 WebPageInfo
     * @return WebPageInfo
     */
    public WebPageInfo toWebPageInfo() {
      WebPageInfo info = new WebPageInfo();
      info.setName(this.title);
      info.setUrl(this.url);
      info.setSnippet(this.content);
      info.setSummary(this.rawContent);
      info.setSiteIcon(this.favicon);
      return info;
    }
  }
}
