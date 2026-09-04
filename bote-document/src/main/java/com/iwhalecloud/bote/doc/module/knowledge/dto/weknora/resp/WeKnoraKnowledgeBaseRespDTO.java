package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 知识库信息
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Getter
@Setter
public class WeKnoraKnowledgeBaseRespDTO {

  @JsonProperty("id")
  private String id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("tenant_id")
  private Long tenantId;

  @JsonProperty("embedding_model_id")
  private String embeddingModelId;

  @JsonProperty("summary_model_id")
  private String summaryModelId;

  @JsonProperty("rerank_model_id")
  private String rerankModelId;

  @JsonProperty("knowledge_count")
  private Long knowledgeCount;
}
