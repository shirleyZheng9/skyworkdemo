package com.iwhalecloud.bote.doc.module.knowledge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库启用类型 DTO
 *
 * @author qian.sisheng
 * @since 2026-04-10
 */
@Getter
@Setter
@ToString
public class KnowledgeEnabledTypeDTO {
  /** 是否启用DocChain */
  private Boolean docChainEnabled;
  /** 是否启用WeKnora */
  private Boolean weKnoraEnabled;
  /** 是否启用KnowledgeGraph */
  private Boolean knowledgeGraphEnabled;
}
