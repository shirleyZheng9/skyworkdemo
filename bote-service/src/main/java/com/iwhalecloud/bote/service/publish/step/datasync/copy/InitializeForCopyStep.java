package com.iwhalecloud.bote.service.publish.step.datasync.copy;

import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncTableDefinition;
import com.iwhalecloud.bote.dto.datasync.query.CopyDataParams;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 步骤执行器：初始化复制信息
 *
 * @author chen.linfa
 * @since 2025-08-08
 */
public class InitializeForCopyStep extends AbstractDataSyncStep<CopyDataParams> {

  public InitializeForCopyStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public CopyDataParams convertInputParams(Object params) {
    if (params instanceof CopyDataParams) {
      return (CopyDataParams) params;
    }
    return null;
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, CopyDataParams params) {
    DataSyncParams datasyncParams = new DataSyncParams();
    datasyncParams.setTenantId(params.getTenantId());
    datasyncParams.setResetTenantId(params.getResetTenantId());
    datasyncParams.setSpaceId(params.getSpaceId());
    datasyncParams.setSyncAll(params.getSyncAll());
    datasyncParams.setRelatable(params.getRelatable());
    datasyncParams.setCodeAndIds(params.getCodeAndIds());
    datasyncParams.setResetPrimaryKey(params.getResetPrimaryKey());
    // 复制场景，例外部分特殊表
    List<String> ignoreCodes = DataSyncCodeEnum.ignoreForCopy();
    List<DataSyncTableDefinition> definitions = dataSyncService.queryTableDefinition().stream()
      .filter(p -> !ignoreCodes.contains(p.getDataConfigCode())).collect(Collectors.toList());
    // 模板智能体复制 去除掉 知识库、文档库模块表
    if (Boolean.TRUE.equals(params.getResetPrimaryKey())) {
      List<String> ignoreSceneCodes = DataSyncCodeEnum.ignoreForSceneCopy();
      definitions.stream().filter(p -> !ignoreSceneCodes.contains(p.getDataConfigCode())).toList();
    }
    datasyncParams.setDefinitions(definitions);
    DataSyncDirUtil.createWorkspec(datasyncParams);

    step.setOutputJson(JsonUtil.toJsonString(datasyncParams));
    return ResultVO.success();
  }

}
