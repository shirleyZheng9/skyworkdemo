package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.mapper.skill.SkillPageManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：页面
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class SkillPageDifferencePersistence extends BaseRootPersistence<SkillPageDTO> {
  public SkillPageDifferencePersistence(SkillPageManageMapper skillPageManageMapper) {
    // 新增情况
    setAddConsumer(skillPageManageMapper::insertSkillPage);
    // 修改情况
    setModifyConsumer(skillPageManageMapper::updateSkillPage);
  }
}
