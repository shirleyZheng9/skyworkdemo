package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 技能：页面函数 服务
 *
 * @author auto
 * @since 2024-09-15
 */
public interface ISkillPageFuncManageService {

  /**
   * 保存页面函数
   *
   * @param pageFunc 页面函数
   * @return 结果
   */
  ResultVO<SkillPageFuncDTO> saveSkillPageFunc(SkillPageFuncDTO pageFunc);

  /**
   * 查询单个页面函数
   *
   * @param tenantId 租户 ID
   * @param pageFuncId 页面函数主键
   * @return 页面函数
   */
  SkillPageFuncDTO findSkillPageFunc(Long tenantId, Long pageFuncId);

  /**
   * 查询页面函数列表
   *
   * @param queryParams 查询条件
   * @return 页面函数列表
   */
  List<SimpleSkillPageFuncDTO> querySkillPageFuncList(SkillQueryParams queryParams);

  /**
   * 查询页面函数列表（分页）
   *
   * @param queryParams 查询条件
   * @return 页面函数分页列表
   */
  PageInfo<SkillPageFuncDTO> querySkillPageFuncPage(SkillQueryParams queryParams);

  /**
   * 查询页面函数列表（分页）
   *
   * @param queryParams 查询条件
   * @return 页面函数分页列表
   */
  PageInfo<SimpleSkillPageFuncDTO> querySimpleSkillPageFuncPage(SkillQueryParams queryParams);

  /**
   * 删除页面函数
   *
   * @param tenantId 租户 ID
   * @param pageFuncId 页面函数主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillPageFunc(Long tenantId, Long pageFuncId);
}
