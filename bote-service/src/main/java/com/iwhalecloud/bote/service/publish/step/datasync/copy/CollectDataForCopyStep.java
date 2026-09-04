package com.iwhalecloud.bote.service.publish.step.datasync.copy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 步骤执行器：收集数据
 *
 * @author chen.linfa
 * @since 2025-08-08
 */
public class CollectDataForCopyStep extends AbstractDataSyncStep<Object> {

  public CollectDataForCopyStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, Object object) {
    DataSyncParams params = getOutputParams(PublishStepType.INITIALIZE_FOR_COPY, new TypeReference<DataSyncParams>() {
    });
    ResultVO<Void> result = producer.execute(params);
    return result.isSuccess() ? ResultVO.success() : ResultVO.fail(result.getResultMsg());
  }

}
