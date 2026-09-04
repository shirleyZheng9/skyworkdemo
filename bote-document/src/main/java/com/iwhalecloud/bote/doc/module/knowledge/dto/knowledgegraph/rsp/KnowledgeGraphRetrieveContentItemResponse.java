package com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 检索结果单条内容
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeGraphRetrieveContentItemResponse {

  @JsonProperty("chunk_id")
  private String chunkId;

  private String name;

  private String text;

  private Double score;
}
