package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.QuestionClassifierStep;
import com.iwhalecloud.bote.dto.orchestration.step.QuestionClassifierStep.QuestionClassification;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

/**
 * 问题分类步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class QuestionClassifierStepConverter extends AbstractStepConverter<QuestionClassifierStep> {
  public QuestionClassifierStepConverter() {
    super(QuestionClassifierStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, QuestionClassifierStep step, ConverterContext context) {
    step.setModelId(parseRequiredJsonAttr(node, "modelId", "大模型", String.class));
    step.setQuestion(parseRequiredJsonAttr(node, "question", "问题", String.class));
    Long promptId = parseJsonAttr(node, "promptId", Long.class);
    if (promptId != null) {
      step.setPromptId(promptId);
      step.setPromptParameters(parseJsonAttr(node, "promptParameters", new TypeReference<List<ParameterSpec>>() {
      }));
    }
    else {
      step.setInstruction(parseJsonAttr(node, "instruction", String.class));
    }

    // 解析分类
    List<QuestionClassification> classifications = parseRequiredJsonAttr(node, "classifications", "分类列表", new TypeReference<List<QuestionClassification>>() {
    });
    // 校验分类 ID、名称不同为空，不能重复
    for (QuestionClassification classification : classifications) {
      Assert.notNull(classification.getId(), "分类 ID 不能为空");
      Assert.hasLength(classification.getName(), "分类名称不能为空");
    }
    Assert.isTrue(classifications.stream().map(QuestionClassification::getId).distinct().count() == classifications.size(), "分类 ID 不能重复");
    Assert.isTrue(classifications.stream().map(QuestionClassification::getName).distinct().count() == classifications.size(), "分类名称不能重复");

    // 设置各个分类的下一步
    for (Pair<String, SceneGraphNodeDTO> edge : ListUtils.emptyIfNull(context.getTargetEdges(node))) {
      if (StepType.PARALLEL_END.equals(edge.getRight().getNodeType()) || SceneConsts.EXCEPTION_EDGE_PORT.equals(edge.getLeft())) {
        continue;
      }
      // 起始端点的 port 表示分类 ID
      String id = edge.getLeft();
      if ("else".equals(id)) {
        step.setElseStep(edge.getRight().getNodeCode());
      }
      else {
        QuestionClassification classification = IterableUtils.find(classifications, c -> c.getId().toString().equals(id));
        Assert.notNull(classification, () -> "分类 ID " + id + " 不存在");
        classification.setNext(edge.getRight().getNodeCode());
      }
    }

    step.setClassifications(classifications);
    step.setMemory(parseJsonAttr(node, "memory", MemoryConfig.class));
    step.setCustomModelConfig(parseJsonAttr(node, "customModelConfig", CustomModelConfig.class));
  }
}
