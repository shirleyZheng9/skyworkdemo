package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.mapper.skill.SkillPageFuncManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：页面函数
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class SkillPageFuncDifferencePersistence extends BaseRootPersistence<SkillPageFuncDTO> {
  public SkillPageFuncDifferencePersistence(SkillPageFuncManageMapper pageFuncManageMapper) {
    // 新增情况
    setAddConsumer(pageFuncManageMapper::insertSkillPageFunc);
    // 修改情况
    setModifyConsumer(pageFuncManageMapper::updateSkillPageFunc);
  }
}
