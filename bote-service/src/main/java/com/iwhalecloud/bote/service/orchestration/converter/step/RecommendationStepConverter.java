package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.RecommendationStep;
import com.iwhalecloud.bote.dto.orchestration.step.RecommendationStep.RecommendationQuestionItem;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;

/**
 * 推荐步骤转换器
 *
 * @author bianjp
 * @since 2025-07-21
 */
public class RecommendationStepConverter extends AbstractStepConverter<RecommendationStep> {
  public RecommendationStepConverter() {
    super(RecommendationStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, RecommendationStep step, ConverterContext context) {
    String recommendationType = parseRequiredJsonAttr(node, "type", "推荐类型", String.class);
    step.setRecommendationType(recommendationType);
    switch (recommendationType) {
      case SceneConsts.RECOMMENDATION_TYPE_SCENE:
        step.setSceneIds(parseRequiredJsonAttr(node, "sceneIds", "智能体列表", new TypeReference<List<Long>>() {
        }));
        break;
      case SceneConsts.RECOMMENDATION_TYPE_FLOW:
        step.setFlowIds(parseRequiredJsonAttr(node, "flowIds", "工作流列表", new TypeReference<List<Long>>() {
        }));
        break;
      case SceneConsts.RECOMMENDATION_TYPE_PAGE:
        step.setPageIds(parseRequiredJsonAttr(node, "pageIds", "页面列表", new TypeReference<List<Long>>() {
        }));
        break;
      case SceneConsts.RECOMMENDATION_TYPE_QUESTION:
        step.setQuestions(parseRequiredJsonAttr(node, "questions", "问题列表", new TypeReference<List<RecommendationQuestionItem>>() {
        }));
        break;
      default:
        throw new BssException("未知的推荐类型: " + recommendationType);
    }
  }
}
