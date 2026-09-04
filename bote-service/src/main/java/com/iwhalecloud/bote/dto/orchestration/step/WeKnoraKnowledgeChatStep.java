package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora 知识问答步骤
 *
 * @author huangyunming
 * @since 2026-04-01
 */
@Getter
@Setter
public class WeKnoraKnowledgeChatStep extends AbstractStep {

  /** 大模型 ID（字面量或引用表达式） */
  private String modelId;
  /** 问题（模板字符串，支持引用变量） */
  private String question;
  /** WeKnora 知识库 ID（支持模板变量，多个用英文逗号分隔；编排侧不再解析博特知识库缓存） */
  private String weKnoraKnowledgeBaseIds;
  /** 知识库 ID（支持引用变量，支持逗号分隔的多个知识库 ID) */
  private String knowledgeId;
  /** 文档 ID（支持引用变量，支持逗号分隔的多个文档 ID) */
  private String documentId;
  /** 提示词（模板字符串，支持引用变量），可选 */
  private String promptContent;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 是否流式输出；WeKnora 步骤仅支持流式，请为 true 或留空（不可为 false） */
  private Boolean stream;
  /** 是否返回参考文档，默认否 */
  private Boolean withReferences;
  /** 是否返回追问问题，默认否 */
  private Boolean withQuestions;
  /** 是否开启对话上下文 */
  private Boolean withChatLog;
  /** 推理策略 */
  private ThinkingStrategy thinkingStrategy;
  /** 知识库列表（JSON 格式，支持动态参数） */
  private String knowledgeExt;
  /** 文档列表（JSON 格式，支持动态参数） */
  private String resourceExt;
  /** 自定义模型配置 */
  private CustomModelConfig customModelConfig;

  public WeKnoraKnowledgeChatStep() {
    super(StepType.WEKNORA_CHAT);
  }
}
