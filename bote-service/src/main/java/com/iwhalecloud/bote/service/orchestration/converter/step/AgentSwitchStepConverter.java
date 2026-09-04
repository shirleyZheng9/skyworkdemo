package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.AgentSwitchStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * 场景切换 步骤转换器
 *
 * @author zyt 11472359
 * @since 2025-05-28
 */
public class AgentSwitchStepConverter extends AbstractStepConverter<AgentSwitchStep> {
  public AgentSwitchStepConverter() {
    super(AgentSwitchStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, AgentSwitchStep step, ConverterContext context) {
    // 判断 switchMode（切换模式）是自动切换（auto）还是用户选择切换（manual）
    Map<String, Object> nodeData = node.getNodeData();
    String switchMode = MapUtils.getString(nodeData, "switchMode");
    Assert.notNull(switchMode, "切换模式不能为空");
    switch (switchMode) {
      case "auto":
        step.setSceneAppId(parseRequiredJsonAttr(node, "sceneAppId", "智能应用", Long.class));
        step.setSceneId(parseRequiredJsonAttr(node, "sceneId", "智能体", Long.class));
        // 对话内容非必填
        step.setUserMessage(parseJsonAttr(node, "userMessage", String.class));
        step.setSwitchMode("auto");
        step.setParameters(getInputParams(node));
        break;
      case "manual":
        // 用户选择切换 逻辑
        step.setSwitchMode("manual");
        step.setUserPrompt(parseRequiredJsonAttr(node, "userPrompt", "提示语", String.class));
        String agentSource = MapUtils.getString(nodeData, "agentSource");
        Assert.notNull(agentSource, "智能体来源不能为空");
        step.setAgentSource(parseRequiredJsonAttr(node, "agentSource", "智能体来源", String.class));
        switch (agentSource) {
          case "customize":
            // 自定义场景
            step.setScenes(parseRequiredJsonAttr(node, "scenes", "智能体列表", new TypeReference<List<AgentSwitchStep.SceneInfo>>() { }));
            break;
          case "reference":
            // 引用参数
            step.setReferenceScenes(parseRequiredJsonAttr(node, "referenceScenes", "引用变量参数", String.class));
            break;
          case "intentRecognition":
            // 意图识别
            step.setIntentRecognitionMessage(parseRequiredJsonAttr(node, "intentRecognitionMessage", "意图识别消息", String.class));
            // 判断是否传入botId
            if (nodeData.containsKey("botId")) {
               step.setBotId(parseRequiredJsonAttr(node, "botId", "智能应用", Long.class));
            }
            break;
          default:
            throw new BssException("不是正确的智能体来源：" + agentSource);
        }
        break;
      default:
        throw new BssException("不是正确的切换模式：" + switchMode);
    }

  }
}
