package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.SkillFlowParamDTO;
import com.iwhalecloud.bote.mapper.skill.SkillFlowManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：流程参数
 *
 * @author auto
 * @since 2024-09-18
 */
@Component
public final class SkillFlowParamDifferencePersistence extends BaseRootPersistence<SkillFlowParamDTO> {

  public SkillFlowParamDifferencePersistence(SkillFlowManageMapper flowManageMapper) {
    setAddConsumer(flowManageMapper::insertSkillFlowParam);
    setModifyConsumer(flowManageMapper::updateSkillFlowParam);
  }

}
