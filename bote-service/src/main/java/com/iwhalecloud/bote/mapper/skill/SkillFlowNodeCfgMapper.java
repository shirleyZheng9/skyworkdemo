package com.iwhalecloud.bote.mapper.skill;

import com.iwhalecloud.bote.dto.skill.SkillFlowNodeCfgDTO;
import java.util.List;

/**
 * 技能流程节点配置 Mapper
 *
 * @author lizuyin
 * @since 2025-12-26
 */
public interface SkillFlowNodeCfgMapper {
  /**
   * 查询所有有效状态的节点配置（包括根节点和非根节点）
   *
   * @return 节点配置列表
   */
  List<SkillFlowNodeCfgDTO> selectAllNodeConfigs();
}

