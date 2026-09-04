package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.mapper.skill.AgentSkillMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：Agent Skill
 *
 * @author bianjp
 * @since 2026-02-03
 */
@Component
public final class AgentSkillDifferencePersistence extends BaseRootPersistence<AgentSkillDTO> {
  public AgentSkillDifferencePersistence(AgentSkillMapper agentSkillMapper) {
    // 新增情况
    setAddConsumer(agentSkillMapper::insertAgentSkill);
    // 修改情况
    setModifyConsumer(agentSkillMapper::updateAgentSkill);
  }
}
