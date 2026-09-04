package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.mapper.base.LabelManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：关联标签
 *
 * @author chen.linfa
 * @since 2024-09-11
 */
@Component
public final class LabelObjectRelDifferencePersistence extends BaseRootPersistence<LabelObjectRelDTO> {
  public LabelObjectRelDifferencePersistence(LabelManageMapper labelManageMapper) {
    // 新增情况
    this.setBatchAddConsumer(labelManageMapper::batchInsertLabelObjectRel);
    // 修改情况
    this.setModifyConsumer(labelManageMapper::updateLabelObjectRel);
  }
}
