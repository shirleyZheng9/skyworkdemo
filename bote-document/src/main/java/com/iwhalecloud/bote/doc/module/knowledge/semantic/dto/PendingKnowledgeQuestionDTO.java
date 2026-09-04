package com.iwhalecloud.bote.doc.module.knowledge.semantic.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 待补充语义标准问法的问答记录
 *
 * @author qian.sisheng
 * @since 2026/04/02
 */
@Getter
@Setter
@ToString
public class PendingKnowledgeQuestionDTO {
  /** 问答主键 */
  private Long qaId;
  /** 租户ID */
  private Long tenantId;
  /** 消息ID */
  private String sessionId;
  /** 用户问题原文 */
  private String question;
}
