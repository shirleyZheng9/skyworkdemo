package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillAttrSpecDTO;
import com.iwhalecloud.bote.mapper.skill.SkillAttrManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：属性规格
 *
 * @author chen.linfa
 * @since 2024-09-15
 */
@Component
public final class SkillAttrSpecDifferencePersistence extends BaseRootPersistence<SkillAttrSpecDTO> {
  public SkillAttrSpecDifferencePersistence(SkillAttrManageMapper attrManageMapper) {
    // 新增情况
    this.setAddConsumer(attrManageMapper:: insertAttrSpec);
    // 修改情况
    this.setModifyConsumer(attrManageMapper::updateAttrSpec);
  }

}
