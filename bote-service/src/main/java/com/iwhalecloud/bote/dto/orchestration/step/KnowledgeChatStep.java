package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识问答步骤
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
public class KnowledgeChatStep extends AbstractStep {
  /** 大模型 ID（字面量或引用表达式） */
  private String modelId;
  /** 问题（模板字符串，支持引用变量） */
  private String question;
  /** 知识库 ID（支持引用变量，支持逗号分隔的多个知识库 ID) */
  private String knowledgeId;
  /** 文档 ID（支持引用变量，支持逗号分隔的多个文档 ID) */
  private String documentId;
  /** 提示词（模板字符串，支持引用变量），可选 */
  private String promptContent;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 是否流式输出，默认否 */
  private Boolean stream;
  /** 是否返回参考文档，默认否 */
  private Boolean withReferences;
  /** 是否返回追问问题，默认否 */
  private Boolean withQuestions;
  /** 是否开启对话上下文 */
  private Boolean withChatLog;
  /** 推理策略 */
  private ThinkingStrategy thinkingStrategy;
  /** 知识库列表(外系统接入-百应-知识中台) 需支持动态参数,所以设置为字符串类型 */
  private String knowledgeExt;
  /** 文档列表(外系统接入-知识中台) 需支持动态参数,所以设置为字符串类型*/
  private String resourceExt;
  /** 自定义模型配置 */
  private CustomModelConfig customModelConfig;

  public KnowledgeChatStep() {
    super(StepType.KNOWLEDGE_CHAT);
  }
}
