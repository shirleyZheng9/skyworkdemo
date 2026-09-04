package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.mapper.portal.TenantSettingInfoManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：意图识别租户设置信息日志
 *
 * @author auto
 * @since 2024-12-18
 */
@Component
public final class TenantSettingInfoDifferencePersistence extends BaseRootPersistence<TenantSettingInfoDTO> {

  public TenantSettingInfoDifferencePersistence(TenantSettingInfoManageMapper tenantSettingInfoManageMapper) {
    setAddConsumer(tenantSettingInfoManageMapper::insertTenantSettingInfo);
    setModifyConsumer(tenantSettingInfoManageMapper::updateTenantSettingInfo);
  }

}
