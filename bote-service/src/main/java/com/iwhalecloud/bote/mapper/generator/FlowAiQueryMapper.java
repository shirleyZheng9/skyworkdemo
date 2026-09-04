package com.iwhalecloud.bote.mapper.generator;

import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.dto.generator.flow.SkillBasicInfoDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 流程 AI 辅助功能相关的数据库查询
 *
 * @author bianjp
 * @since 2025-03-31
 */
public interface FlowAiQueryMapper {
  /**
   * 批量查询工作流
   */
  List<SkillFlowDTO> selectFlowsByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询工作流的基本信息
   */
  List<SkillBasicInfoDTO> selectFlowInfoByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 查询单个工作流
   */
  SkillFlowDTO selectFlowById(@Param("tenantId") Long tenantId, @Param("id") Long id);

  /**
   * 批量查询 API 服务
   */
  List<SkillServiceDTO> selectApiServicesByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询 API 服务的基本信息
   */
  List<SkillBasicInfoDTO> selectApiServiceInfoByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 查询单个 API 服务
   */
  SkillServiceDTO selectApiServiceById(@Param("tenantId") Long tenantId, @Param("id") Long id);

  /**
   * 批量查询 SQL 服务
   */
  List<SkillSqlDTO> selectSqlServicesByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询 SQL 服务的基本信息
   */
  List<SkillBasicInfoDTO> selectSqlServiceInfoByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 查询单个 SQL 服务
   */
  SkillSqlDTO selectSqlServiceById(@Param("tenantId") Long tenantId, @Param("id") Long id);

  /**
   * 批量查询服务函数
   */
  List<SkillFunctionDTO> selectFunctionsByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询服务函数的基本信息
   */
  List<SkillBasicInfoDTO> selectFunctionInfoByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 查询单个服务函数
   */
  SkillFunctionDTO selectFunctionById(@Param("tenantId") Long tenantId, @Param("id") Long id);

  /**
   * 批量查询插件
   */
  List<SkillPluginDTO> selectPluginsByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询插件的基本信息
   */
  List<SkillBasicInfoDTO> selectPluginInfoByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询页面
   */
  List<SkillPageDTO> selectPagesByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询页面的基本信息
   */
  List<SkillBasicInfoDTO> selectPageInfoByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询页面函数
   */
  List<SkillPageFuncDTO> selectPageFuncListByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询页面函数的基本信息
   */
  List<SkillBasicInfoDTO> selectPageFuncInfoByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询知识库
   */
  List<KnowledgeBaseDTO> selectKnowledgeListByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询知识库的基本信息
   */
  List<SkillBasicInfoDTO> selectKnowledgeInfoByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 批量查询MCP
   */
  List<SkillBasicInfoDTO> selectMcpInfoByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);
}
