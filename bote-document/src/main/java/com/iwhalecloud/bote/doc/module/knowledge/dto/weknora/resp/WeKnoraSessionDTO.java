package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 会话信息
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Getter
@Setter
public class WeKnoraSessionDTO {

  @JsonProperty("id")
  private String id;

  @JsonProperty("knowledge_base_id")
  private String knowledgeBaseId;

  @JsonProperty("max_rounds")
  private Integer maxRounds;

  @JsonProperty("enable_rewrite")
  private Boolean enableRewrite;

  @JsonProperty("fallback_strategy")
  private String fallbackStrategy;

  @JsonProperty("fallback_response")
  private String fallbackResponse;

  @JsonProperty("summary_model_id")
  private String summaryModelId;

  @JsonProperty("rerank_model_id")
  private String rerankModelId;
}
