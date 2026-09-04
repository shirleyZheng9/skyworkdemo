package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.PromptDTO;
import com.iwhalecloud.bote.mapper.skill.PromptManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：提示语
 *
 * @author qian.sisheng
 * @since 2024/8/2
 */
@Component
public final class PromptDifferencePersistence extends BaseRootPersistence<PromptDTO> {
  public PromptDifferencePersistence(PromptManageMapper promptManageMapper) {
    // 新增情况
    setAddConsumer(promptManageMapper::insertPrompt);
    // 修改情况
    setModifyConsumer(promptManageMapper::updatePrompt);
  }

}
