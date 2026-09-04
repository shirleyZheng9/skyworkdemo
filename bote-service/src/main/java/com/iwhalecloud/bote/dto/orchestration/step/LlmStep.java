package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.model.VisionConfig;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class LlmStep extends AbstractStep {
  /** 大模型 ID（字面量或引用表达式） */
  private String modelId;
  /** 消息列表（至少要有一条，第一条必须是 system，后面的只能是 user/assistant） */
  private List<LlmMessage> messages;
  /** 提示词 ID, 可选 */
  private Long promptId;
  /** 提示词参数 */
  private List<ParameterSpec> promptParameters;
  /** 用户消息内容（模板字符串，支持引用变量），非必填 */
  private String userMessage;
  /** 视觉配置，可选 */
  private VisionConfig vision;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 是否流式输出 */
  private Boolean stream;
  /** 推理策略 */
  private ThinkingStrategy thinkingStrategy;
  /** 自定义模型配置 */
  private CustomModelConfig customModelConfig;

  public LlmStep() {
    super(StepType.LLM);
  }

  /**
   * 消息
   */
  @Getter
  @Setter
  @ToString
  public static class LlmMessage {
    /** 角色 */
    private MessageRole role;
    /** 提示词 ID, 可选 */
    private Long promptId;
    /** 提示词参数 */
    private List<ParameterSpec> promptParameters;
    /** 消息内容，支持引用变量 */
    private String content;
  }

}
