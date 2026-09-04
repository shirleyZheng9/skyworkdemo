package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillPageCompDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageCompQueryParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：页面组件 MAPPER
 *
 * @author lizuyin
 * @since 2026-01-14
 */
public interface SkillPageCompManageMapper {

  /**
   * 根据主键获取页面组件
   *
   * @param tenantId 租户 ID
   * @param pageCompId 页面组件主键
   * @return 页面组件
   */
  SkillPageCompDTO getSkillPageComp(@Param("tenantId") Long tenantId, @Param("id") Long pageCompId);

  /**
   * 新增页面组件
   *
   * @param dto 页面组件
   * @return 结果
   */
  int insertSkillPageComp(@Param("dto") SkillPageCompDTO dto);

  /**
   * 修改页面组件
   *
   * @param dto 页面组件
   * @return 结果
   */
  int updateSkillPageComp(@Param("dto") SkillPageCompDTO dto);

  /**
   * 删除页面组件（逻辑删除）
   *
   * @param tenantId 租户 ID
   * @param pageCompId 页面组件主键
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteSkillPageComp(@Param("tenantId") Long tenantId, @Param("pageCompId") Long pageCompId, @Param("updatorId") Long updatorId);

  /**
   * 获取页面组件列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 页面组件分页列表
   */
  Page<SkillPageCompDTO> selectSkillPageCompPage(@Param("query") SkillPageCompQueryParams queryParams, RowBounds rowBounds);

  /**
   * 检验页面组件名称唯一性
   *
   * @param dto 页面组件
   * @return 是否存在同名
   */
  Boolean existsSkillPageCompName(@Param("dto") SkillPageCompDTO dto);
}

