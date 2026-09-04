package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.EntityInfoDTO;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 实体全局搜索
 *
 * @author qian.sisheng
 * @since 2025-12-04
 */
public interface EntityGlobalSearchMapper {

  /**
   * 根据智能体ID列表查询智能体信息
   *
   * @param tenantId 租户ID
   * @param ids 智能体ID列表
   * @return 智能体信息
   */
  List<EntityInfoDTO> selectBotByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据场景ID查询场景信息
   *
   * @param tenantId 租户ID
   * @param ids 场景ID
   * @return 场景信息
   */
  List<EntityInfoDTO> selectSceneByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据API ID列表查询API信息
   *
   * @param tenantId 租户ID
   * @param ids API ID列表
   * @return API信息
   */
  List<EntityInfoDTO> selectSkillServiceByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据工作流ID列表查询工作流信息
   *
   * @param tenantId 租户ID
   * @param ids 工作流ID列表
   * @return 工作流信息
   */
  List<EntityInfoDTO> selectSkillFlowByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据页面ID列表查询页面信息
   *
   * @param tenantId 租户ID
   * @param ids 页面ID列表
   * @return 页面信息
   */
  List<EntityInfoDTO> selectSkillPageByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据服务函数ID列表查询服务函数I信息
   *
   * @param tenantId 租户ID
   * @param ids 服务函数ID列表
   * @return 服务函数I信息
   */
  List<EntityInfoDTO> selectSkillFunctionByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据静态数据ID列表查询静态数据信息
   *
   * @param tenantId 租户ID
   * @param ids 静态数据ID列表
   * @return 静态数据信息
   */
  List<EntityInfoDTO> selectSkillAttrByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据大模型插件ID列表查询大模型插件信息
   *
   * @param tenantId 租户ID
   * @param ids 大模型插件ID列表
   * @return 大模型插件信息
   */
  List<EntityInfoDTO> selectSkillPluginByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据页面函数ID列表查询页面函数信息
   *
   * @param tenantId 租户ID
   * @param ids 页面函数ID列表
   * @return 页面函数信息
   */
  List<EntityInfoDTO> selectSkillPageFuncByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据SQL ID列表查询SQL信息
   *
   * @param tenantId 租户ID
   * @param ids SQL ID列表
   * @return SQL信息
   */
  List<EntityInfoDTO> selectSkillSqlByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据知识ID列表查询知识信息
   *
   * @param tenantId 租户ID
   * @param ids 知识ID列表
   * @return 知识信息
   */
  List<EntityInfoDTO> selectKnowledgeByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据大模型ID列表查询大模型信息
   *
   * @param tenantId 租户ID
   * @param ids 大模型ID列表
   * @return 大模型信息
   */
  List<EntityInfoDTO> selectLargeModelByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据数据表ID列表查询数据表信息
   *
   * @param tenantId 租户ID
   * @param ids 数据表ID列表
   * @return 数据表信息
   */
  List<EntityInfoDTO> selectDataTableByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据提示语ID列表查询提示语信息
   *
   * @param tenantId 租户ID
   * @param ids 提示语ID列表
   * @return 提示语信息
   */
  List<EntityInfoDTO> selectPromptByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据数据源ID列表查询数据源信息
   *
   * @param tenantId 租户ID
   * @param ids 数据源ID列表
   * @return 数据源信息
   */
  List<EntityInfoDTO> selectDataSourceByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据MCP ID列表查询MCP信息
   *
   * @param tenantId 租户ID
   * @param ids MCP ID列表
   * @return MCP信息
   */
  List<EntityInfoDTO> selectMcpByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据A2A ID列表查询A2A信息
   *
   * @param tenantId 租户ID
   * @param ids A2A ID列表
   * @return A2A信息
   */
  List<EntityInfoDTO> selectA2AByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据副驾指令ID列表查询副驾指令信息
   *
   * @param tenantId 租户ID
   * @param ids 副驾指令ID列表
   * @return 副驾指令信息
   */
  List<EntityInfoDTO> selectCopilotPointByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据服务平台ID列表查询服务平台信息
   *
   * @param tenantId 租户ID
   * @param ids 服务平台ID列表
   * @return 服务平台信息
   */
  List<EntityInfoDTO> selectServicePlatformByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据任务ID列表查询任务信息
   *
   * @param tenantId 租户ID
   * @param ids 任务ID列表
   * @return 任务信息
   */
  List<EntityInfoDTO> selectJobByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据插件ID列表查询插件信息
   *
   * @param tenantId 租户ID
   * @param ids 插件ID列表
   * @return 插件信息
   */
  List<EntityInfoDTO> selectPluginByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据环境ID列表查询环境信息
   *
   * @param tenantId 租户ID
   * @param ids 环境ID列表
   * @return 环境信息
   */
  List<EntityInfoDTO> selectEnvByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 根据API ID列表查询agenSkill信息
   *
   * @param tenantId 租户ID
   * @param ids agentSill ID列表
   * @return API信息
   */
  List<EntityInfoDTO> selectAgentSkillByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

}
