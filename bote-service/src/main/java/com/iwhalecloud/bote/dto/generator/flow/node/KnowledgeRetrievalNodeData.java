package com.iwhalecloud.bote.dto.generator.flow.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 知识检索节点数据
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class KnowledgeRetrievalNodeData {
  /** 知识库 ID */
  private String knowledgeId;
  /** 问题（模板字符串，支持引用变量） */
  private String question;
  /** 召回数量（默认为 10） */
  private Integer topK;
}
