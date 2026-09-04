package com.iwhalecloud.bote.doc.module.knowledge.semantic.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 会话提问向量近邻命中项
 *
 * @author qian.sisheng
 * @since 2026/04/02
 */
@Getter
@Setter
public class KnowledgeQuestionDTO {
  /** 消息ID */
  private String msgId;
  /** 问题 */
  private String question;
  /** 标准问题 */
  private String standardQuestion;
  /** 得分 */
  private float score;
}
