package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep.LlmMessage;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import com.iwhalecloud.bote.mapper.skill.SkillQueryMapper;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 大模型步骤转换器抽象类
 *
 * @author bianjp
 * @since 2026-02-01
 */
public abstract class AbstractLlmStepConverter<T extends AbstractStep> extends AbstractStepConverter<T> {
  private static final SkillQueryMapper skillQueryMapper = SpringUtil.getBean(SkillQueryMapper.class);

  public AbstractLlmStepConverter(Supplier<T> stepFactory) {
    super(stepFactory);
  }

  /**
   * 解析消息列表
   */
  protected final List<LlmMessage> parseMessages(SceneGraphNodeDTO node) {
    List<LlmMessage> messages = parseJsonAttr(node, "messages", new TypeReference<>() {
    });
    if (messages == null || messages.isEmpty()) {
      return List.of();
    }
    for (LlmMessage message : messages) {
      if (message.getPromptId() != null) {
        message.setContent(null);
      }
      else {
        message.setPromptParameters(null);
      }
    }
    return messages;
  }

  /**
   * 解析提示词
   */
  protected final void parsePrompt(SceneGraphNodeDTO node,
                                   Consumer<Long> promptIdSetter,
                                   Consumer<List<ParameterSpec>> promptParametersSetter,
                                   Consumer<String> promptSetter,
                                   String promptKey,
                                   String promptTitle) {
    Long promptId = parseJsonAttr(node, "promptId", Long.class);
    // 引用提示词
    if (promptId != null) {
      promptIdSetter.accept(promptId);
      promptParametersSetter.accept(parseJsonAttr(node, "promptParameters", new TypeReference<>() {
      }));
    }
    // 手动输入的提示词
    else {
      promptSetter.accept(parseRequiredJsonAttr(node, promptKey, promptTitle, String.class));
    }
  }

  /**
   * 解析技能列表
   */
  @Nullable
  protected final List<LlmSkillItem> parseSkills(SceneGraphNodeDTO node, List<String> allowedSkillTypes, Long tenantId) {
    List<LlmSkillItem> skills = parseJsonAttr(node, "skills", new TypeReference<>() {
    });
    if (skills == null || skills.isEmpty()) {
      return null;
    }
    // 校验技能
    validateSkills(tenantId, skills, allowedSkillTypes);
    // 处理技能参数
    processSkillParameters(skills);
    return skills;
  }

  /**
   * 校验技能
   */
  private void validateSkills(Long tenantId, List<LlmSkillItem> skills, List<String> allowedSkillTypes) {
    for (LlmSkillItem skill : skills) {
      Assert.hasLength(skill.getSkillType(), "技能类型不能为空");
      Assert.notNull(skill.getSkillId(), "技能 ID 不能为空");
      Assert.isTrue(allowedSkillTypes.contains(skill.getSkillType()), () -> "不支持的技能类型: " + skill.getSkillType());
    }
    // 校验工作流不能是对话型
    List<Long> flowIds = skills.stream()
      .filter(s -> StepType.WORKFLOW.equals(s.getSkillType()))
      .map(LlmSkillItem::getSkillId).collect(Collectors.toList());
    if (!flowIds.isEmpty()) {
      Assert.isTrue(!skillQueryMapper.existsChatflowByIds(tenantId, flowIds), "大模型节点的技能不支持对话型工作流");
    }
  }

  /**
   * 处理技能参数
   */
  private void processSkillParameters(List<LlmSkillItem> skills) {
    // 未配置对模型隐藏的参数时，删除参数结构，以减少存储空间占用，并避免参数结构与技能的最新入参结构不一致
    for (LlmSkillItem skill : skills) {
      if (StepType.MCP.equals(skill.getSkillType())) {
        if (MapUtils.isNotEmpty(skill.getMcpToolParameters())) {
          skill.setMcpToolParameters(skill.getMcpToolParameters().entrySet().stream()
            .filter(e -> hasModelInvisibleParam(e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
      }
      else if (skill.getParameters() != null && !hasModelInvisibleParam(skill.getParameters())) {
        skill.setParameters(null);
      }
    }
  }

  /**
   * 是否存在对模型隐藏的参数
   */
  private boolean hasModelInvisibleParam(ParameterSpec spec) {
    if (Boolean.FALSE.equals(spec.getModelVisible())) {
      return true;
    }
    // 只对对象类型进行递归处理，列表类型不处理（不支持给列表元素赋值）
    if (spec.isObject() && spec.hasChildren()) {
      for (ParameterSpec child : spec.getChildren()) {
        if (hasModelInvisibleParam(child)) {
          return true;
        }
      }
    }
    return false;
  }
}
