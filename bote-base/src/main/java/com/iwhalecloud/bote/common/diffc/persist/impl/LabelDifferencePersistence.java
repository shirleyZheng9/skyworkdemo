package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.base.LabelDTO;
import com.iwhalecloud.bote.mapper.base.LabelManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：标签
 *
 * @author chen.linfa
 * @since 2024-09-11
 */
@Component
public final class LabelDifferencePersistence extends BaseRootPersistence<LabelDTO> {
  public LabelDifferencePersistence(LabelManageMapper labelManageMapper) {
    // 新增情况
    this.setAddConsumer(labelManageMapper::insertLabel);
    // 修改情况
    this.setModifyConsumer(labelManageMapper::updateLabel);
  }
}
