package com.iwhalecloud.bote.service.publish.step.datasync.online;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;

/**
 * 步骤执行器：收集数据并封装导出结果（在线发布）
 *
 * @author lizuyin
 * @since 2026-01-21
 */
public class CollectDataOnlineStep extends AbstractDataSyncStep<Object> {

  public CollectDataOnlineStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public DataSyncParams convertInputParams(Object params) {
    if (params == null) {
      return null;
    }
    return JsonUtil.parseJson(JsonUtil.toJsonString(params), DataSyncParams.class);
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, Object object) {
    // 1. 从初始化步骤获取数据同步参数
    Map<String, Object> initOutput = getOutputParams(PublishStepType.INITIALIZE_ONLINE, new TypeReference<>() {
    });
    if (initOutput == null) {
      return ResultVO.fail("未找到初始化步骤的输出参数");
    }
    Object dataSyncParamsObj = initOutput.get("dataSyncParams");
    if (dataSyncParamsObj == null) {
      return ResultVO.fail("初始化步骤未返回数据同步参数");
    }
    DataSyncParams params = JsonUtil.parseJson(JsonUtil.toJsonString(dataSyncParamsObj),
      DataSyncParams.class);
    if (params == null) {
      return ResultVO.fail("数据同步参数解析失败");
    }

    // 2. 收集数据
    ResultVO<Void> collectResult = producer.execute(params);
    if (!collectResult.isSuccess()) {
      return ResultVO.fail(collectResult.getResultMsg());
    }

    return ResultVO.success();
  }

}

