package com.iwhalecloud.bote.mapper.skill;

import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 技能查询
 *
 * <p>仅供技能工具转换器使用，只查出需要的字段(id, name, code, 入参)</p>
 *
 * @author bianjp
 * @since 2025-04-17
 */
public interface SkillQueryMapper {
  /**
   * 批量查询工作流
   */
  List<SkillFlowDTO> selectFlowsByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 检查是否有对话型工作流
   */
  boolean existsChatflowByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 批量查询 API 服务
   */
  List<SkillServiceDTO> selectApiServicesByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 批量查询 SQL 服务
   */
  List<SkillSqlDTO> selectSqlServicesByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 批量查询服务函数
   */
  List<SkillFunctionDTO> selectFunctionsByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 批量查询插件
   */
  List<SkillPluginDTO> selectPluginsByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 批量查询页面
   */
  List<SkillPageDTO> selectPagesByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 批量查询页面函数
   */
  List<SkillPageFuncDTO> selectPageFuncListByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

  /**
   * 批量查询知识库
   */
  List<KnowledgeBaseDTO> selectKnowledgeListByIds(@Param("tenantId") Long tenantId, @Param("ids") Collection<Long> ids);

}
