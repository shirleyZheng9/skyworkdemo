package com.iwhalecloud.bote.service.publish.step.eval;

import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.model.ModelEvalDTO;
import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.dto.model.request.EvalRequest;
import com.iwhalecloud.bote.service.model.IModelFinetuneManageService;
import com.iwhalecloud.bote.service.model.helper.SmallModelAnswerHelper;
import com.iwhalecloud.bote.service.publish.step.AbstractPublishStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.springframework.util.Assert;

/**
 * 步骤执行器：执行微调训练
 *
 * @author chen.linfa
 * @since 2025-02-18
 */
public class CallEvalStep extends AbstractPublishStep<ModelEvalDTO> {

  private static final SmallModelAnswerHelper helper = SpringUtil.getBean(SmallModelAnswerHelper.class);

  private static final IModelFinetuneManageService finetuneService = SpringUtil.getBean(IModelFinetuneManageService.class);

  public CallEvalStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public ModelEvalDTO convertInputParams(Object params) {
    if (params instanceof ModelEvalDTO) {
      return (ModelEvalDTO) params;
    }
    return null;
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, ModelEvalDTO eval) {
    ModelFinetuneDTO finetune = finetuneService.getFinetune(eval.getModelId());
    EvalRequest request = new EvalRequest();
    request.setPublishId(record.getObjId().toString());
    request.setTenantId(record.getTenantId().toString());
    request.setBaseModelName(finetune.getBaseModelSubType());
    request.setFineTuningModelId(finetune.getId().toString());
    // 超参设置
    request.setThreshold(finetune.getThreshold());
    request.setMaxLength(finetune.getMaxLength());
    request.setBatchSize(finetune.getBatchSize());
    // 附件
    FileInfoVO fileInfo = fileService.getFileInfoById(eval.getRequestFileId());
    Assert.notNull(fileInfo, () -> "文件不存在: id=" + eval.getRequestFileId());
    request.setFilePath(fileInfo.getFilePathInServer());
    request.setFileServer(fileInfo.getStoreType());

    ResultVO<Void> result = helper.eval(request);
    step.setOutputJson(JsonUtil.toJsonString(request));
    return result.isSuccess() ? ResultVO.success() : ResultVO.fail(result.getResultMsg());
  }

}
