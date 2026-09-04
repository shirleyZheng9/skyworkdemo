package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：租户
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Component
public final class TenantDifferencePersistence extends BaseRootPersistence<TenantDTO> {
  public TenantDifferencePersistence(TenantManageMapper tenantManageMapper) {
    // 新增情况
    this.setAddConsumer(tenantManageMapper::insertTenant);
    // 修改情况
    this.setModifyConsumer(tenantManageMapper::updateTenant);
  }

}
