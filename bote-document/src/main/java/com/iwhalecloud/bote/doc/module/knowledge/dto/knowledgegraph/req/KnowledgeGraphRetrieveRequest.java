package com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * POST /api/retrieve 请求体
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeGraphRetrieveRequest {

  private String database;

  private String query;

  @JsonProperty("top_k")
  private Integer topK;

  @JsonProperty("score_threshold")
  private Double scoreThreshold;
}
