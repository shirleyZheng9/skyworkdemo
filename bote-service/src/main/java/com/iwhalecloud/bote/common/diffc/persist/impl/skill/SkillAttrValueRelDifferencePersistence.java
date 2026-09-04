package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bote.dto.skill.SkillAttrValueRelDTO;
import com.iwhalecloud.bote.mapper.skill.SkillAttrManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * SkillAttrValueRelDTO
 *
 * @author qian.sisheng
 * @since 2025-1-16
 */
@Component
public final class SkillAttrValueRelDifferencePersistence extends BaseRootPersistence<SkillAttrValueRelDTO> {
  public SkillAttrValueRelDifferencePersistence(SkillAttrManageMapper attrManageMapper) {
    // 新增情况
    this.setBatchAddConsumer(attrManageMapper::batchInsertAttrValueRel);
    // 修改情况
    this.setModifyConsumer(attrManageMapper::updateAttrValueRel);
  }
}
