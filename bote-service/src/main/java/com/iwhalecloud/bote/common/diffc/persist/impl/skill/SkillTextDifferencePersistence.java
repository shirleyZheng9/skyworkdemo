package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillTextDTO;
import com.iwhalecloud.bote.mapper.skill.SkillTextManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：文本
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class SkillTextDifferencePersistence extends BaseRootPersistence<SkillTextDTO> {
  public SkillTextDifferencePersistence(SkillTextManageMapper textManageMapper) {
    // 新增情况
    setAddConsumer(textManageMapper::insertSkillText);
    // 修改情况
    setModifyConsumer(textManageMapper::updateSkillText);
  }
}
