package com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * POST /api/retrieve 响应 payload
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeGraphRetrieveResponse {

  private List<KnowledgeGraphRetrieveContentItemResponse> contents;
}
