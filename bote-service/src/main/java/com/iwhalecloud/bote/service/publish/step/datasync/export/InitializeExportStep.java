package com.iwhalecloud.bote.service.publish.step.datasync.export;

import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.dto.datasync.query.ExportDataParams;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;

/**
 * 步骤执行器：初始化导出信息
 *
 * @author chen.linfa
 * @since 2024-10-21
 */
public class InitializeExportStep extends AbstractDataSyncStep<ExportDataParams> {

  public InitializeExportStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public ExportDataParams convertInputParams(Object params) {
    if (params instanceof ExportDataParams) {
      return (ExportDataParams) params;
    }
    return null;
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, ExportDataParams params) {
    DataSyncParams datasyncParams = new DataSyncParams();
    datasyncParams.setTenantId(params.getTenantId());
    datasyncParams.setSyncAll(params.getSyncAll());
    datasyncParams.setRelatable(params.getRelatable());
    datasyncParams.setCodeAndIds(params.getCodeAndIds());
    datasyncParams.setExportFileName(params.getExportFileName());
    datasyncParams.setBackUp(params.getBackUp());
    datasyncParams.setDefinitions(dataSyncService.queryTableDefinition());
    DataSyncDirUtil.createWorkspec(datasyncParams);

    step.setOutputJson(JsonUtil.toJsonString(datasyncParams));
    return ResultVO.success();
  }

}
