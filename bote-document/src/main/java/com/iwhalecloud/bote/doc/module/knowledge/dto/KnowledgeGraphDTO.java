package com.iwhalecloud.bote.doc.module.knowledge.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 知识库简要信息
 *
 * @author qian.sisheng
 * @since 2026-04-14
 */
@Getter
@Setter
public class KnowledgeGraphDTO {
  /** 知识库ID */
  private String knowledgeId;
  /** 知识库名称 */
  private String knowledgeName;
}
