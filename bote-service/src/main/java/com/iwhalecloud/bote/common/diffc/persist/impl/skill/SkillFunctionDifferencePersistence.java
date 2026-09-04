package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.mapper.skill.SkillFunctionManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：服务函数
 *
 * @author auto
 * @since 2024-09-15
 */
@Component
public final class SkillFunctionDifferencePersistence extends BaseRootPersistence<SkillFunctionDTO> {
  public SkillFunctionDifferencePersistence(SkillFunctionManageMapper functionManageMapper) {
    // 新增情况
    setAddConsumer(functionManageMapper::insertSkillFunction);
    // 修改情况
    setModifyConsumer(functionManageMapper::updateSkillFunction);
  }
}
