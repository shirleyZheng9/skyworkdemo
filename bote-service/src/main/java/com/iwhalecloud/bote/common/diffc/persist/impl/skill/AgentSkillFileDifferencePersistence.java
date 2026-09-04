package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bote.dto.skill.AgentSkillFileDTO;
import com.iwhalecloud.bote.mapper.skill.AgentSkillFileMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存：Agent Skill 文件
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
@Component
public final class AgentSkillFileDifferencePersistence extends BaseRootPersistence<AgentSkillFileDTO> {
  public AgentSkillFileDifferencePersistence(AgentSkillFileMapper agentSkillFileMapper) {
    setAddConsumer(agentSkillFileMapper::insertAgentSkillFile);
    setModifyConsumer(agentSkillFileMapper::updateAgentSkillFile);
  }
}
