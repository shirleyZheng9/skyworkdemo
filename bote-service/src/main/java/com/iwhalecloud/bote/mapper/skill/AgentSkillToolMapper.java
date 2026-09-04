package com.iwhalecloud.bote.mapper.skill;

import com.iwhalecloud.bote.dto.skill.AgentSkillToolDTO;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillToolDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Agent Skill Tool 相关数据库操作
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
public interface AgentSkillToolMapper {
  List<SimpleAgentSkillToolDTO> selectToolsBySkillId(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId);

  List<SimpleAgentSkillToolDTO> selectToolsBySkillIds(@Param("tenantId") Long tenantId, @Param("skillIds") List<Long> skillIds);

  /**
   * 逻辑删除技能下全部槽位关联
   */
  int deleteBySkillId(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId, @Param("updatorId") Long updatorId);

  /**
   * 批量插入槽位关联
   */
  int batchInsertAgentSkillTools(@Param("list") List<AgentSkillToolDTO> list);
}
