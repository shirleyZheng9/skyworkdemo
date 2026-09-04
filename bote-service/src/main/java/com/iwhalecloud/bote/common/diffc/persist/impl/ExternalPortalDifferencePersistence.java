package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.portal.ExternalPortalDTO;
import com.iwhalecloud.bote.mapper.portal.ExternalPortalMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：外部门户
 *
 * @author bianjp
 * @since 2025-02-24
 */
@Component
public final class ExternalPortalDifferencePersistence extends BaseRootPersistence<ExternalPortalDTO> {
  public ExternalPortalDifferencePersistence(ExternalPortalMapper externalPortalMapper) {
    // 新增情况
    this.setAddConsumer(externalPortalMapper::insertPortal);
    // 修改情况
    this.setModifyConsumer(externalPortalMapper::updatePortal);
  }

}
