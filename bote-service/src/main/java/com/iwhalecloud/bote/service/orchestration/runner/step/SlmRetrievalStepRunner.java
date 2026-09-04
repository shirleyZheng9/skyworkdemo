package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.dto.model.request.PredictRequest;
import com.iwhalecloud.bote.dto.model.response.PredictResponse.PredictInfo;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.SlmRetrievalStep;
import com.iwhalecloud.bote.mapper.model.ModelFinetuneManageMapper;
import com.iwhalecloud.bote.service.model.helper.SmallModelAnswerHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 微调模型检索步骤执行器
 *
 * @author chen.linfa
 * @since 2025-04-19
 */
public class SlmRetrievalStepRunner extends AbstractLlmStepRunner<SlmRetrievalStep> {

  private static final ModelFinetuneManageMapper modelFinetuneManageMapper = SpringUtil.getBean(ModelFinetuneManageMapper.class);

  private static final SmallModelAnswerHelper smallModelAnswerHelper = SpringUtil.getBean(SmallModelAnswerHelper.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, SlmRetrievalStep step) {
    String question = resolveTemplate(step.getQuestion());
    ModelFinetuneDTO finetune = modelFinetuneManageMapper.getFinetune(step.getModelId());
    Assert.notNull(finetune, () -> "微调模型不存在: " + step.getModelId());
    Assert.isTrue(BaseConsts.FINETUNE_STATUS_PUBLISH.equals(finetune.getStatus()), () -> "微调模型未启用：" + step.getModelId());
    PredictRequest request = new PredictRequest();
    request.setTenantId(finetune.getTenantId().toString());
    request.setBaseModelName(finetune.getBaseModelSubType());
    request.setFineTuningModelId(finetune.getId().toString());
    request.setThreshold(finetune.getThreshold());
    request.setMaxLength(finetune.getMaxLength());
    request.setText(question);
    ResultVO<PredictInfo> result = smallModelAnswerHelper.chat(request);
    Assert.isTrue(result.isSuccess(), () -> "微调模型执行异常：" + result.getResultMsg());
    Map<String, Object> output = new HashMap<>();
    output.put("label", result.getResultObject().getLabel());
    output.put("score", result.getResultObject().getScore());
    output.put("message", result.getResultObject().getMessage());
    context.setStepInputLog(ImmutableMap.of("modelId", step.getModelId(), "question", StringUtils.defaultString(question)));
    context.setStepOutput(step, output);
  }

}
