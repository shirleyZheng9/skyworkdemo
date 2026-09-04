package com.iwhalecloud.bote.service.publish.step.datasync.online;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.dto.datasync.query.OnlinePublishParams;
import com.iwhalecloud.bote.dto.publish.PublishGatewaySimpleDTO;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.publish.IPublishGatewayManageService;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 步骤执行器：初始化在线发布信息
 *
 * @author lizuyin
 * @since 2026-01-21
 */
public class InitializeOnlineStep extends AbstractDataSyncStep<OnlinePublishParams> {

  private static final IPublishGatewayManageService gatewayManageService = SpringUtil.getBean(IPublishGatewayManageService.class);

  public InitializeOnlineStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public OnlinePublishParams convertInputParams(Object params) {
    if (params instanceof OnlinePublishParams) {
      return (OnlinePublishParams) params;
    }
    return null;
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, OnlinePublishParams params) {
    // 1. 验证发布参数
    if (params.getGatewayId() == null) {
      return ResultVO.fail("网关ID不能为空");
    }

    // 2. 验证并获取网关信息（此处服务接口保证返回非空网关信息，否则会抛出异常）
    PublishGatewaySimpleDTO gateway = gatewayManageService.findPublishGatewaySimple(record.getTenantId(), params.getGatewayId());
    if (StringUtils.isEmpty(gateway.getGatewayUrl())) {
      return ResultVO.fail("网关URL不能为空");
    }

    // 3. 初始化数据同步参数
    DataSyncParams datasyncParams = new DataSyncParams();
    datasyncParams.setTenantId(params.getTenantId());
    datasyncParams.setSyncAll(params.getSyncAll());
    datasyncParams.setRelatable(params.getRelatable());
    datasyncParams.setCodeAndIds(params.getCodeAndIds());
    datasyncParams.setSpaceId(params.getSpaceId());
    datasyncParams.setDefinitions(dataSyncService.queryTableDefinition());
    DataSyncDirUtil.createWorkspec(datasyncParams);

    // 4. 保存数据同步参数和验证后的网关信息到输出
    step.setOutputJson(JsonUtil.toJsonString(ImmutableMap.of("dataSyncParams", datasyncParams, "gateway", gateway)));
    return ResultVO.success();
  }

}

