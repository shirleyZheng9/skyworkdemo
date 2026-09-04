package com.iwhalecloud.bote.service.publish.step.intent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.model.request.FinetuneRequest;
import com.iwhalecloud.bote.service.model.helper.SmallModelAnswerHelper;
import com.iwhalecloud.bote.service.publish.step.AbstractPublishStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 步骤执行器：执行微调训练
 *
 * @author chen.linfa
 * @since 2025-02-18
 */
public class CallFinetuneStep extends AbstractPublishStep<Object> {

  private static final SmallModelAnswerHelper helper = SpringUtil.getBean(SmallModelAnswerHelper.class);

  public CallFinetuneStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, Object object) {
    FinetuneRequest request = getOutputParams(PublishStepType.INITIALIZE_FINETUNE, new TypeReference<FinetuneRequest>() {
    });
    ResultVO<Void> result = helper.finetune(request);
    return result.isSuccess() ? ResultVO.success() : ResultVO.fail(result.getResultMsg());
  }
}
