package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.portal.PrivDTO;
import com.iwhalecloud.bote.mapper.portal.PrivManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：权限
 *
 * @author chen.linfa
 * @since 2024-10-14
 */
@Component
public final class PrivDifferencePersistence extends BaseRootPersistence<PrivDTO> {
  public PrivDifferencePersistence(PrivManageMapper privManageMapper) {
    // 新增情况
    this.setAddConsumer(privManageMapper::insertPriv);
    // 修改情况
    this.setModifyConsumer(privManageMapper::updatePriv);
  }
}
