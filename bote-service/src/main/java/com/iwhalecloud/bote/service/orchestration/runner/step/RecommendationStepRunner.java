package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.RecommendationStep;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 推荐步骤执行器
 *
 * @author bianjp
 * @since 2025-07-21
 */
public class RecommendationStepRunner extends AbstractLlmStepRunner<RecommendationStep> {
  @Override
  protected void doRun(SceneOrchestrationContext context, RecommendationStep step) {
    String recommendationType = step.getRecommendationType();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("type", recommendationType);
    switch (StringUtils.defaultString(recommendationType)) {
      case SceneConsts.RECOMMENDATION_TYPE_SCENE:
        data.put("sceneIds", step.getSceneIds());
        break;
      case SceneConsts.RECOMMENDATION_TYPE_FLOW:
        data.put("flowIds", step.getFlowIds());
        break;
      case SceneConsts.RECOMMENDATION_TYPE_PAGE:
        data.put("pageIds", step.getPageIds());
        break;
      case SceneConsts.RECOMMENDATION_TYPE_QUESTION:
        data.put("questions", step.getQuestions());
        break;
      default:
        throw new BssException("未知的推荐类型: " + step.getType());
    }
    context.getReplyHandler().reply(ChatMessageType.RECOMMENDATION, data, step.getCode(), step.getName());
  }
}
