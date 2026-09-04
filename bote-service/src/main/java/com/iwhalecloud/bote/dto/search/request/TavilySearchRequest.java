package com.iwhalecloud.bote.dto.search.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Tavily 搜索请求 DTO
 *
 * @author wangtingyun
 * @since 2026-03-30
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TavilySearchRequest {
  /** API 密钥 */
  private String apiKey;
  /** 搜索查询内容 */
  private String query;
  /** 搜索深度（basic/advanced） */
  @JsonProperty("search_depth")
  private String searchDepth;
  /** 最大返回结果数量 (可选，默认 5) */
  @JsonProperty("max_results")
  private Integer maxResults;
  /** 是否包含 AI 生成的简短答案 (可选) */
  @JsonProperty("include_answer")
  private Boolean includeAnswer;
  /** 是否包含图像 (可选) */
  @JsonProperty("include_images")
  private Boolean includeImages;
}
