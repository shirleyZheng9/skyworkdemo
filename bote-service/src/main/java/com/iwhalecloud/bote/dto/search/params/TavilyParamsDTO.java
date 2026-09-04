package com.iwhalecloud.bote.dto.search.params;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Tavily 搜索参数配置 DTO
 *
 * @author wangtingyun
 * @since 2026-03-30
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TavilyParamsDTO {
  /** API Key */
  private String apiKey;
  /** 请求地址 */
  private String url;
  /** 搜索深度（basic/advanced） */
  private String searchDepth;
  /** 包含的答案数量 */
  private Integer maxResults;
  /** 是否包含答案 */
  private Boolean includeAnswer;
  /** 是否包含图像 */
  private Boolean includeImages;
}
