package com.iwhalecloud.bote.adapter.dify.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库详情
 *
 * @author qian.sisheng
 * @since 2025-10-14
 */

@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class KnowledgeDetailDTO {
  /** 检索模型 */
  private RetrievalModelDictDTO retrievalModelDict;
}
