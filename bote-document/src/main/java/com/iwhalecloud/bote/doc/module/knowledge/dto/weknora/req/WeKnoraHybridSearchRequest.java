package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 单知识库混合检索请求体（对应 {@code GET /v1/knowledge-bases/{id}/hybrid-search}，带 JSON body）
 *
 * @see <a href="https://github.com/Tencent/WeKnora/blob/main/docs/api/knowledge-base.md">WeKnora API</a>
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeKnoraHybridSearchRequest {

  @JsonProperty("query_text")
  private String queryText;

  @JsonProperty("vector_threshold")
  private Double vectorThreshold;

  @JsonProperty("keyword_threshold")
  private Double keywordThreshold;

  @JsonProperty("match_count")
  private Integer matchCount;

  @JsonProperty("disable_keywords_match")
  private Boolean disableKeywordsMatch;

  @JsonProperty("disable_vector_match")
  private Boolean disableVectorMatch;
}
