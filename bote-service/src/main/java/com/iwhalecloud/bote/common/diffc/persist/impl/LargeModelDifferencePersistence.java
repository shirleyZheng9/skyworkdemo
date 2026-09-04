package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import org.springframework.stereotype.Component;

/**
 * 大模型
 *
 * @author qian.sisheng
 * @since 2024/8/7
 */
@Component
public final class LargeModelDifferencePersistence extends BaseRootPersistence<LargeModelDTO> {
  public LargeModelDifferencePersistence(LargeModelManageMapper modelManageMapper) {
    setAddConsumer(modelManageMapper::insertLargeModel);
    setModifyConsumer(modelManageMapper::updateLargeModel);
  }
}
