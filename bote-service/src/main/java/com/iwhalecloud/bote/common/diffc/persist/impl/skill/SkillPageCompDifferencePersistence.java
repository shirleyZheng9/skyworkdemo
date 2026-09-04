package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bote.dto.skill.SkillPageCompDTO;
import com.iwhalecloud.bote.mapper.skill.SkillPageCompManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：页面组件
 *
 * <p>
 * 负责在 DataDifferenceStarter.computeSave 调用时，将新增映射到 insert，修改映射到 update。
 * </p>
 *
 * @author lizuyin
 * @since 2026-01-14
 */
@Component
public final class SkillPageCompDifferencePersistence extends BaseRootPersistence<SkillPageCompDTO> {

  public SkillPageCompDifferencePersistence(SkillPageCompManageMapper mapper) {
    setAddConsumer(mapper::insertSkillPageComp);
    setModifyConsumer(mapper::updateSkillPageComp);
  }
}

