package com.iwhalecloud.bote.doc.module.knowledge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库简单信息
 *
 * @author qian.sisheng
 * @since 2026-04-09
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeBaseSimpleDTO {
  /** 知识库ID */
  private String knowledgeId;
  /** 知识库名称 */
  private String knowledgeName;
  /** 知识库类型 */
  private String knowledgeType;
  /** 知识库图标 */
  private String knowledgeIcon;
  /** 知识库文档数量 */
  private Long fileCounts;
  /** 知识库状态 */
  private String knowledgeStatus;
  /** 知识库描述 */
  private String knowledgeDesc;
}
