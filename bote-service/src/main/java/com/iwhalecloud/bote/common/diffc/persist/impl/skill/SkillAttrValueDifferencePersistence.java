package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillAttrValueDTO;
import com.iwhalecloud.bote.mapper.skill.SkillAttrManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：属性值规格
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class SkillAttrValueDifferencePersistence extends BaseRootPersistence<SkillAttrValueDTO> {
  public SkillAttrValueDifferencePersistence(SkillAttrManageMapper attrManageMapper) {
    // 新增情况
    this.setBatchAddConsumer(attrManageMapper::batchInsertAttrValue);
    // 修改情况
    this.setModifyConsumer(attrManageMapper::updateAttrValue);
  }

}
