package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.google.common.collect.ImmutableList;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.model.VisionConfig;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep.CustomMcpService;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep.DynamicSkillsConfig;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep.FileProcessingStrategy;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import java.util.List;
import org.springframework.util.Assert;

/**
 * Agent 步骤转换器
 *
 * @author bianjp
 * @since 2025-05-24
 */
public class AgentStepConverter extends AbstractLlmStepConverter<AgentStep> {
  /** 支持的技能类型(只支持普通技能，不支持页面、页面函数) */
  public static final List<String> ALLOWED_SKILL_TYPES = ImmutableList.of(
    StepType.SERVICE,
    StepType.SQL,
    StepType.LLM_SKILL,
    StepType.PLUGIN,
    StepType.MCP,
    StepType.TOOLBOX,
    StepType.WORKFLOW,
    StepType.KNOWLEDGE_CHAT,
    StepType.INVOKE_SCENE
  );

  public AgentStepConverter() {
    super(AgentStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, AgentStep step, ConverterContext context) {
    step.setModelId(parseRequiredJsonAttr(node, "modelId", "大模型", String.class));
    parsePrompt(node, step::setPromptId, step::setPromptParameters, step::setPromptContent, "promptContent", "提示词");
    step.setShowToolInvocation(parseJsonAttr(node, "showToolInvocation", Boolean.class));
    step.setVision(parseJsonAttr(node, "vision", VisionConfig.class));
    step.setToolUseRulesPrompt(parseJsonAttr(node, "toolUseRulesPrompt", String.class));
    step.setSkills(parseSkills(node, ALLOWED_SKILL_TYPES, context.getTenantId()));
    DynamicSkillsConfig dynamicSkillsConfig = parseJsonAttr(node, "dynamicSkillsConfig", DynamicSkillsConfig.class);
    if (dynamicSkillsConfig != null && Boolean.TRUE.equals(dynamicSkillsConfig.getEnabled())) {
      Assert.hasLength(dynamicSkillsConfig.getSkills(), "动态技能列表不能为空");
      Assert.isTrue(dynamicSkillsConfig.getSkills().startsWith("$."), "动态技能列表只能引用其它参数，不能手动赋值");
    }
    step.setDynamicSkillsConfig(dynamicSkillsConfig);
    CustomMcpService customMcpService = parseJsonAttr(node, "customMcpService", CustomMcpService.class);
    if (customMcpService != null && Boolean.TRUE.equals(customMcpService.getEnabled())) {
      Assert.hasLength(customMcpService.getServerName(), "自定义 MCP 服务名称不能为空");
      Assert.hasLength(customMcpService.getServerType(), "自定义 MCP 服务类型不能为空");
      Assert.hasLength(customMcpService.getServerUrl(), "自定义 MCP 服务地址不能为空");
      step.setCustomMcpService(customMcpService);
    }
    step.setFileProcessingStrategy(parseJsonAttr(node, "fileProcessingStrategy", FileProcessingStrategy.class));
    step.setMemory(parseJsonAttr(node, "memory", MemoryConfig.class));
    step.setCustomModelConfig(parseJsonAttr(node, "customModelConfig", CustomModelConfig.class));
  }

}
