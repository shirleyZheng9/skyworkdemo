package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.SkillPageCompDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageCompQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 技能：页面组件服务
 *
 * @author lizuyin
 * @since 2026-01-14
 */
public interface ISkillPageCompManageService {

  /**
   * 查询单个页面组件
   *
   * @param tenantId 租户 ID
   * @param pageCompId 页面组件主键
   * @return 页面组件
   */
  SkillPageCompDTO findSkillPageComp(Long tenantId, Long pageCompId);

  /**
   * 保存页面组件（新增或更新）
   *
   * @param dto 页面组件
   * @return 结果
   */
  ResultVO<SkillPageCompDTO> saveSkillPageComp(SkillPageCompDTO dto);

  /**
   * 删除页面组件
   *
   * @param tenantId 租户 ID
   * @param pageCompId 页面组件主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillPageComp(Long tenantId, Long pageCompId);

  /**
   * 查询页面组件列表（分页）
   *
   * @param queryParams 查询条件
   * @return 页面组件分页列表
   */
  PageInfo<SkillPageCompDTO> querySkillPageCompPage(SkillPageCompQueryParams queryParams);
}

