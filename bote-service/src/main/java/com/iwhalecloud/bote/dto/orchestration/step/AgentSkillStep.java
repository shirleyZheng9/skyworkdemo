package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep.LlmMessage;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Agent Skill 步骤
 *
 * @author bianjp
 * @since 2026-02-03
 */
@Getter
@Setter
public class AgentSkillStep extends AbstractStep {
  /** 大模型 ID（字面量或引用表达式） */
  private String modelId;
  /** 自定义模型配置 */
  private CustomModelConfig customModelConfig;
  /** 消息列表（至少要有一条，第一条必须是 system，后面的只能是 user/assistant） */
  private List<LlmMessage> messages;
  /** 提示词 ID, 可选 */
  private Long promptId;
  /** 提示词参数 */
  private List<ParameterSpec> promptParameters;
  /** 用户消息内容（模板字符串，支持引用变量），非必填 */
  private String userMessage;
  /** 技能列表 */
  private List<LlmSkillItem> skills;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 是否流式输出 */
  private Boolean stream;

  public AgentSkillStep() {
    super(StepType.AGENT_SKILL);
  }
}
