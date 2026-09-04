package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillObjectDTO;
import com.iwhalecloud.bote.mapper.skill.SkillObjectManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：对象
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class SkillObjectDifferencePersistence extends BaseRootPersistence<SkillObjectDTO> {
  public SkillObjectDifferencePersistence(SkillObjectManageMapper objectManageMapper) {
    // 新增情况
    setAddConsumer(objectManageMapper::insertSkillObject);
    // 修改情况
    setModifyConsumer(objectManageMapper::updateSkillObject);
  }
}
