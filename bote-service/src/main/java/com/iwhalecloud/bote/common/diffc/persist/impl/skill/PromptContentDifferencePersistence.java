package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bote.dto.skill.PromptContentDTO;
import com.iwhalecloud.bote.mapper.skill.PromptManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * @author qian.sisheng
 * @since 2025-2-13
 */
@Component
public final class PromptContentDifferencePersistence extends BaseRootPersistence<PromptContentDTO> {
  public PromptContentDifferencePersistence(PromptManageMapper promptManageMapper) {
    // 新增情况
    setBatchAddConsumer(promptManageMapper::batchInsertPromptContent);
    // 修改情况
    setModifyConsumer(promptManageMapper::updatePromptContent);
  }
}
