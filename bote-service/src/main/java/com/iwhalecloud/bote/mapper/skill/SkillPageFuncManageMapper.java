package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：页面函数 Mapper
 *
 * @author auto
 * @since 2024-09-15
 */
public interface SkillPageFuncManageMapper {
  /**
   * 新增页面函数
   *
   * @param pageFunc 页面函数
   * @return 结果
   */
  int insertSkillPageFunc(@Param("dto") SkillPageFuncDTO pageFunc);

  /**
   * 修改页面函数
   *
   * @param pageFunc 页面函数
   * @return 结果
   */
  int updateSkillPageFunc(@Param("dto") SkillPageFuncDTO pageFunc);

  /**
   * 根据主键获取页面函数
   *
   * @param tenantId 租户 ID
   * @param pageFuncId 页面函数主键
   * @return 页面函数
   */
  SkillPageFuncDTO getSkillPageFunc(@Param("tenantId") Long tenantId, @Param("id") Long pageFuncId);

  /**
   * 查询页面函数的简单信息
   */
  SkillPageFuncDTO selectSimplePageFunc(@Param("tenantId") Long tenantId, @Param("id") Long pageFuncId);

  /**
   * 批量查询页面函数的简单信息
   */
  List<SkillPageFuncDTO> selectSimplePageFuncList(@Param("tenantId") Long tenantId, @Param("ids") List<Long> pageFuncIds);

  /**
   * 查找页面函数列表
   *
   * @param params 查询参数
   * @return 页面函数列表
   */
  List<SkillPageFuncDTO> selectSkillPageFuncList(@Param("query") SkillQueryParams params);

  /**
   * 查找页面函数列表 (分页)
   *
   * @param params 查询参数
   * @return 页面函数分页列表
   */
  Page<SkillPageFuncDTO> selectSkillPageFuncPage(@Param("query") SkillQueryParams params, RowBounds rowBounds);

  /**
   * 根据函数编码查找页面函数
   *
   * @param tenantId 租户ID
   * @param funcCode 函数编码
   * @return 页面函数列表
   */
  List<SkillPageFuncDTO> selectSkillPageFuncByCode(@Param("tenantId") Long tenantId, @Param("funcCode") String funcCode);

  /**
   * 删除页面函数
   */
  int deleteSkillPageFunc(@Param("tenantId") Long tenantId, @Param("pageFuncId") Long pageFuncId, @Param("updatorId") Long updatorId);

  /**
   * 检验页面函数编码唯一性
   *
   * @param pageFunc 页面函数
   * @return 结果
   */
  Boolean existsSkillPageFuncCode(@Param("dto") SkillPageFuncDTO pageFunc);
}
