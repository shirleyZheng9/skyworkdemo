package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.mapper.model.ModelFinetuneManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：模型微调
 *
 * @author auto
 * @since 2025-03-03
 */
@Component
public final class ModelFinetuneDifferencePersistence extends BaseRootPersistence<ModelFinetuneDTO> {

  public ModelFinetuneDifferencePersistence(ModelFinetuneManageMapper modelFinetuneManageMapper) {
    setAddConsumer(modelFinetuneManageMapper::insertFinetune);
    setModifyConsumer(modelFinetuneManageMapper::updateFinetune);
  }

}
