package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能项
 *
 * @author bianjp
 * @since 2025-04-24
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class LlmSkillItem {
  /** 技能类型 */
  private String skillType;
  /** 技能 ID */
  private Long skillId;
  /** 外部技能 ID */
  private String extSkillId;
  /** MCP/插件 服务选中的工具名称列表 */
  private List<String> toolNames;
  /** 是否是平台级技能 */
  private Boolean platform;
  /** 知识库名称,对接外部系统使用 百应 知识中台 */
  private String knowledgeName;
  /** 文档名称,对接外部系统使用 知识中台 */
  private String docName;
  /** 知识类型,对接外部系统使用 */
  private String knowledgeType;
  /** 数据库 服务授权的操作类型 */
  private List<String> databaseAlloweds;
  /** 插件，是否启用插件市场 */
  private Boolean isPluginHub;

  // 入参配置，用于 Agent 节点给技能的部分参数赋值
  /** 技能入参。适用于非 MCP 类型的技能 */
  private ParameterSpec parameters;
  /** MCP 技能的入参。key 为工具名称，value 为该工具的入参 */
  private Map<String, ParameterSpec> mcpToolParameters;
}
