package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
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
public interface SkillFunctionManageMapper {
  /**
   * 新增服务函数
   *
   * @param function 服务函数
   * @return 结果
   */
  int insertSkillFunction(@Param("dto") SkillFunctionDTO function);

  /**
   * 修改服务函数
   *
   * @param function 服务函数
   * @return 结果
   */
  int updateSkillFunction(@Param("dto") SkillFunctionDTO function);

  /**
   * 根据主键获取服务函数
   *
   * @param tenantId 租户 ID
   * @param funcId 服务函数主键
   * @return 服务函数
   */
  SkillFunctionDTO getSkillFunction(@Param("tenantId") Long tenantId, @Param("id") Long funcId);

  /**
   * 查询服务函数的简单信息
   */
  SkillFunctionDTO selectSimpleFunction(@Param("tenantId") Long tenantId, @Param("id") Long funcId);

  /**
   * 批量查询服务函数的简单信息
   */
  List<SkillFunctionDTO> selectSimpleFunctions(@Param("tenantId") Long tenantId, @Param("ids") List<Long> funcIds);

  /**
   * 获取服务函数列表（分页）
   *
   * @param queryParams 查询条件
   * @return 服务函数分页列表
   */
  Page<SkillFunctionDTO> selectSkillFunctionPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 删除服务函数
   */
  int deleteSkillFunction(@Param("tenantId") Long tenantId, @Param("funcId") Long funcId, @Param("updatorId") Long updatorId);

  /**
   * 校验服务函数的编码唯一性
   *
   * @param function 服务函数
   * @return 结果l
   */
  boolean existsSkillFunctionCode(@Param("dto") SkillFunctionDTO function);
}
