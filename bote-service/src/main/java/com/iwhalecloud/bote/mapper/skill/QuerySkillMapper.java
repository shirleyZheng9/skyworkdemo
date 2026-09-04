package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.plugin.PluginDTO;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能查询
 *
 * <p>提供给其他模块引用，只查出需要的字段(id, name, code, desc, 入参)</p>
 *
 * @author chen.linfa
 * @since 2025-07-30
 */
public interface QuerySkillMapper {

  /**
   * 获取 API 列表
   *
   * @param queryParams 查询条件
   * @return API 列表
   */
  List<SimpleSkillServiceDTO> selectSkillServiceList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取 API 列表（分页）
   *
   * @param queryParams 查询条件
   * @return API 分页列表
   */
  Page<SimpleSkillServiceDTO> selectSkillServicePage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询单个 API
   *
   * @param tenantId 租户 ID
   * @param serviceId 服务 ID
   * @return API
   */
  SimpleSkillServiceDTO getSkillService(@Param("tenantId") Long tenantId, @Param("serviceId") Long serviceId);

  /**
   * 获取 SQL 列表
   *
   * @param queryParams 查询条件
   * @return SQL 列表
   */
  List<SimpleSkillSqlDTO> selectSkillSqlList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取SQL服务列表（分页）
   *
   * @param queryParams 查询条件
   * @return SQL服务分页列表
   */
  Page<SimpleSkillSqlDTO> selectSkillSqlPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询单个 SQL
   *
   * @param tenantId 租户 ID
   * @param serviceId 服务 ID
   * @return API
   */
  SimpleSkillSqlDTO getSkillSql(@Param("tenantId") Long tenantId, @Param("serviceId") Long serviceId);

  /**
   * 获取插件列表（分页）
   *
   * @param queryParams 查询条件
   * @return 插件分页列表
   */
  Page<SimpleSkillPluginDTO> selectSkillPluginPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询单个 插件
   *
   * @param tenantId 租户 ID
   * @param apiId 插件 ID
   * @return 插件
   */
  SimpleSkillPluginDTO getSkillPlugin(@Param("tenantId") Long tenantId, @Param("apiId") Long apiId);

  /**
   * 查询单个插件
   */
  PluginDTO getPlugin(@Param("tenantId") Long tenantId, @Param("pluginId") Long pluginId);

  /**
   * 获取服务函数列表
   *
   * @param queryParams 查询条件
   * @return 服务函数列表
   */
  List<SimpleSkillFunctionDTO> selectSkillFunctionList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取服务函数列表（分页）
   *
   * @param queryParams 查询条件
   * @return 服务函数分页列表
   */
  Page<SimpleSkillFunctionDTO> selectSkillFunctionPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询单个服务函数
   *
   * @param tenantId 租户 ID
   * @param funcId 函数 ID
   * @return 函数
   */
  SimpleSkillFunctionDTO getSkillFunction(@Param("tenantId") Long tenantId, @Param("funcId") Long funcId);

  /**
   * 获取流程列表
   *
   * @param queryParams 查询条件
   * @return 流程列表
   */
  List<SimpleSkillFlowDTO> selectSkillFlowList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取流程列表（分页）
   *
   * @param queryParams 查询条件
   * @return 流程分页列表
   */
  Page<SimpleSkillFlowDTO> selectSkillFlowPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询单个流程
   *
   * @param tenantId 租户 ID
   * @param flowId 流程 ID
   * @return 流程
   */
  SimpleSkillFlowDTO getSkillFlow(@Param("tenantId") Long tenantId, @Param("flowId") Long flowId);

  /**
   * 获取页面列表
   *
   * @param queryParams 查询条件
   * @return 页面列表
   */
  List<SimpleSkillPageDTO> selectSkillPageList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取页面列表（分页）
   *
   * @param queryParams 查询条件
   * @return 页面分页列表
   */
  Page<SimpleSkillPageDTO> selectSkillPagePage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询单个页面
   *
   * @param tenantId 租户 ID
   * @param pageId 页面 ID
   * @return 页面
   */
  SimpleSkillPageDTO getSkillPage(@Param("tenantId") Long tenantId, @Param("pageId") Long pageId);

  /**
   * 查找页面函数列表
   *
   * @param params 查询参数
   * @return 页面函数列表
   */
  List<SimpleSkillPageFuncDTO> selectSkillPageFuncList(@Param("query") SkillQueryParams params);

  /**
   * 查找页面函数列表 (分页)
   *
   * @param params 查询参数
   * @return 页面函数分页列表
   */
  Page<SimpleSkillPageFuncDTO> selectSkillPageFuncPage(@Param("query") SkillQueryParams params, RowBounds rowBounds);

  /**
   * 查询单个页面函数
   *
   * @param tenantId 租户 ID
   * @param pageFuncId 页面函数 ID
   * @return 页面
   */
  SimpleSkillPageFuncDTO getSkillPageFunc(@Param("tenantId") Long tenantId, @Param("pageFuncId") Long pageFuncId);

  /**
   * 查询单个 agent skill
   *
   * @param tenantId 租户 ID
   * @param skillId 技能 ID
   * @return agent skill
   */
  SimpleAgentSkillDTO getAgentSkill(@Param("tenantId") Long tenantId, @Param("skillId") Long skillId);
}
