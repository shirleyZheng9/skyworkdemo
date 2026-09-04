package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.mapper.skill.SkillFlowManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：流程
 *
 * @author auto
 * @since 2024-09-18
 */
@Component
public final class SkillFlowDifferencePersistence extends BaseRootPersistence<SkillFlowDTO> {

  public SkillFlowDifferencePersistence(SkillFlowManageMapper flowManageMapper) {
    setAddConsumer(flowManageMapper::insertSkillFlow);
    setModifyConsumer(flowManageMapper::updateSkillFlow);
  }

}
