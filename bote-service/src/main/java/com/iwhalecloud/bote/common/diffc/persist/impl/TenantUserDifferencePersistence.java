package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * @author qian.sisheng
 * @since 2024-12-9
 */
@Component
public final class TenantUserDifferencePersistence extends BaseRootPersistence<TenantUserDTO> {

  public TenantUserDifferencePersistence(TenantManageMapper tenantManageMapper) {
    setBatchAddConsumer(tenantManageMapper::batchInsertTenantUser);
    setModifyConsumer(tenantManageMapper::updateTenantUser);
  }
}
