package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.mapper.skill.SkillServiceManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：API
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class SkillServiceDifferencePersistence extends BaseRootPersistence<SkillServiceDTO> {
  public SkillServiceDifferencePersistence(SkillServiceManageMapper serviceManageMapper) {
    // 新增情况
    setAddConsumer(serviceManageMapper::insertSkillService);
    // 修改情况
    setModifyConsumer(serviceManageMapper::updateSkillService);
  }
}
