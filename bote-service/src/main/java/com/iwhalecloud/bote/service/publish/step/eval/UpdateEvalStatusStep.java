package com.iwhalecloud.bote.service.publish.step.eval;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.model.response.UpdatePublishStatusResponse;
import com.iwhalecloud.bote.mapper.model.ModelFinetuneManageMapper;
import com.iwhalecloud.bote.service.publish.step.AbstractPublishStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.math.BigDecimal;

/**
 * 步骤执行器：更新微调训练信息
 *
 * @author chen.linfa
 * @since 2025-02-18
 */
public class UpdateEvalStatusStep extends AbstractPublishStep<UpdatePublishStatusResponse> {

  private static final ModelFinetuneManageMapper finetuneMapper = SpringUtil.getBean(ModelFinetuneManageMapper.class);

  public UpdateEvalStatusStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public UpdatePublishStatusResponse convertInputParams(Object params) {
    if (params == null) {
      return null;
    }
    return JsonUtil.parseJson(JsonUtil.toJsonString(params), UpdatePublishStatusResponse.class);
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, UpdatePublishStatusResponse response) {
    if (auto) {
      // 手工环节，跳过
      return ResultVO.success("break");
    }
    // 记录评测结果
    BigDecimal accuracy = new BigDecimal("0.00");
    String status;
    if (response.isSuccess()) {
      status = BaseConsts.EVAL_STATUS_FINISH;
      accuracy = response.getResultObject().getAccuracy();
    }
    else {
      status = BaseConsts.EVAL_STATUS_FAILED;
    }
    finetuneMapper.updateEvalResult(record.getObjId(), status, accuracy);

    if (!response.isSuccess()) {
      return ResultVO.fail(response.getResultMsg());
    }
    step.setOutputJson(JsonUtil.toJsonString(response.getResultObject()));
    return ResultVO.success("finished");
  }
}
