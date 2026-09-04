package com.iwhalecloud.bote.adapter.dify.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 搜索请求参数
 *
 * @author qian.sisheng
 * @since 2025-10-14
 */
@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class KnowledgeSearchRequest {
  /** 检索模型 */
  private RetrievalModelDictDTO retrievalModel;
  /** 搜索的文本 */
  private String query;

}
