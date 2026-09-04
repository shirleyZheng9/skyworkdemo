package com.iwhalecloud.bote.dto.agent;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户 + 用户维度通用智能体配置
 *
 * @author chen.linfa
 * @since 2026-03-10
 */
@Getter
@Setter
@ToString
public class SimpleAiDefinitionDTO {
  /** 租户 ID */
  private Long tenantId;
  /** 应用 ID */
  private Long botId;
  /** 用户 ID */
  private Long userId;
  /** 启用的模型 ID */
  private Long modelId;
  /** 启用的模型归属的租户 ID */
  private Long modelTenantId;
  /** 环境变量 */
  private Map<String, String> envVariables;
  /** 平台级 MCP ID 列表 */
  private List<Long> platformMcpIds;
  /** 用户级 MCP ID 列表 */
  private List<Long> mcpIds;
  /** 平台级 AGENT SKILLS ID 列表 */
  private List<Long> platformAgentSkillIds;
  /** 用户级 AGENT SKILLS ID 列表 */
  private List<Long> agentSkillIds;
  /** 子智能体 ID 列表 */
  private List<Long> subagentSceneIds;
  /** 知识库 ID 列表 */
  private List<Long> knowledgeIds;
}
