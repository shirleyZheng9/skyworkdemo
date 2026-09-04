package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * knowledgeGraph 知识问答步骤
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
public class KnowledgeGraphKnowledgeChatStep extends AbstractStep {
  /** 大模型 ID（字面量或引用表达式） */
  private String modelId;
  /** 问题 */
  private String question;
  /** 知识库名称 */
  private String knowledgeGraphName;
  /** 提示词（模板字符串，支持引用变量），可选 */
  private String promptContent;
  /** 是否流式返回 */
  private Boolean stream;
  /** 是否返回引用 */
  private Boolean withReferences;
  /** 是否返回问题 */
  private Boolean withQuestions;
  /** 是否返回对话日志 */
  private Boolean withChatLog;

  public KnowledgeGraphKnowledgeChatStep() {
    super(StepType.KNOWLEDGE_GRAPH_CHAT);
  }
}
