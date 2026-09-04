package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.common.consts.DataSyncConsts;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.AgentSkillStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent Skill 步骤转换器
 *
 * @author bianjp
 * @since 2026-02-03
 */
public class AgentSkillStepConverter extends AbstractLlmStepConverter<AgentSkillStep> {
  /** 支持的技能类型 */
  private static final List<String> ALLOWED_SKILL_TYPES;

  static {
    ALLOWED_SKILL_TYPES = new ArrayList<>(AgentStepConverter.ALLOWED_SKILL_TYPES);
    ALLOWED_SKILL_TYPES.add(DataSyncConsts.SKILL_TYPE_AGENT_SKILL);
  }

  public AgentSkillStepConverter() {
    super(AgentSkillStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, AgentSkillStep step, ConverterContext context) {
    step.setModelId(parseRequiredJsonAttr(node, "modelId", "大模型", String.class));
    step.setCustomModelConfig(parseJsonAttr(node, "customModelConfig", CustomModelConfig.class));
    step.setMessages(parseMessages(node));
    parsePrompt(node, step::setPromptId, step::setPromptParameters, step::setUserMessage, "userMessage", "消息内容");
    step.setSkills(parseSkills(node, ALLOWED_SKILL_TYPES, context.getTenantId()));
    step.setStream(parseJsonAttr(node, "stream", Boolean.class));
    step.setMemory(parseJsonAttr(node, "memory", MemoryConfig.class));
  }
}
