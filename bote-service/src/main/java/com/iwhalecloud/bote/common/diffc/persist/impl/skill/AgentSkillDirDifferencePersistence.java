package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bote.dto.skill.AgentSkillDirDTO;
import com.iwhalecloud.bote.mapper.skill.AgentSkillDirMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存：Agent Skill 目录
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
@Component
public final class AgentSkillDirDifferencePersistence extends BaseRootPersistence<AgentSkillDirDTO> {
  public AgentSkillDirDifferencePersistence(AgentSkillDirMapper agentSkillDirMapper) {
    setAddConsumer(agentSkillDirMapper::insertAgentSkillDir);
    setModifyConsumer(agentSkillDirMapper::updateAgentSkillDir);
  }
}
