package com.iwhalecloud.bote.agent.skill;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill 槽位信息
 *
 * @author chen.linfa
 * @since 2026-04-30
 */
@Getter
@Setter
@ToString
public class AgentSkillSlotIdGroup {
  /** 租户 ID */
  private Long tenantId;
  /** 槽位 ID 集合，key 是技能类型 */
  private Map<String, List<Long>> slotIdGroups;
  /** 槽位 与 技能编码的 key value */
  private Map<String, String> slotValueMap;
}
