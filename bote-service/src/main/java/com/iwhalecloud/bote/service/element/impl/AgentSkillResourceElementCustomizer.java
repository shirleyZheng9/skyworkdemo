package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.mapper.skill.AgentSkillMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - Agent Skill
 *
 * @author bianjp
 * @since 2026-02-03
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.AGENT_SKILL)
public class AgentSkillResourceElementCustomizer extends AbstractResourceElementCustomizer {
  private final AgentSkillMapper agentSkillMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.AGENT_SKILL.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long skillId) {
    AgentSkillDTO skill = agentSkillMapper.selectAgentSkillById(tenantId, skillId);
    if (skill == null) {
      return List.of();
    }
    List<ResourceElementDTO> elements = new ArrayList<>();
    if (skill.getCatalogItemId() != null) {
      elements.addAll(createCatalogElement(tenantId, skillId, skill.getCatalogItemId()));
    }
    if (skill.getFileInfoId() != null) {
      elements.add(createElement(tenantId, skillId, skill.getFileInfoId(), DataSyncCodeEnum.AGENT_SKILL_FILE.getCode()));
    }
    return elements;
  }
}
