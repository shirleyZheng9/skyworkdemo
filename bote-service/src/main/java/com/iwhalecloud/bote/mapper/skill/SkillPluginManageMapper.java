package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：插件 Mapper
 *
 * @author auto
 * @since 2024-09-21
 */
public interface SkillPluginManageMapper {
  /**
   * 校验插件的编码唯一性
   *
   * @param plugin 插件
   * @return 结果
   */
  boolean existsSkillPluginCode(@Param("dto") SkillPluginDTO plugin);

  /**
   * 根据主键获取插件
   *
   * @param tenantId 租户 ID
   * @param apiId 插件主键
   * @return 插件
   */
  SkillPluginDTO getSkillPlugin(@Param("tenantId") Long tenantId, @Param("id") Long apiId);

  /**
   * 查询插件的简单信息
   */
  SkillPluginDTO selectSimplePlugin(@Param("tenantId") Long tenantId, @Param("id") Long apiId);

  /**
   * 批量查询插件的简单信息
   */
  List<SkillPluginDTO> selectSimplePlugins(@Param("tenantId") Long tenantId, @Param("ids") List<Long> apiIds);

  /**
   * 新增插件
   *
   * @param plugin 插件
   * @return 结果
   */
  int insertSkillPlugin(@Param("dto") SkillPluginDTO plugin);

  /**
   * 修改插件
   *
   * @param plugin 插件
   * @return 结果
   */
  int updateSkillPlugin(@Param("dto") SkillPluginDTO plugin);

  /**
   * 删除插件
   */
  int deleteSkillPlugin(@Param("tenantId") Long tenantId, @Param("apiId") Long apiId, @Param("updatorId") Long updatorId);

  /**
   * 获取插件列表
   *
   * @param queryParams 查询条件
   * @return 插件列表
   */
  List<SkillPluginDTO> selectSkillPluginList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取插件列表（分页）
   *
   * @param queryParams 查询条件
   * @return 插件分页列表
   */
  Page<SkillPluginDTO> selectSkillPluginPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);
}
