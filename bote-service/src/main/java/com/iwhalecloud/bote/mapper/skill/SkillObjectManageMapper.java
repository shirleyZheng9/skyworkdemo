package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillObjectDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：对象 Mapper
 *
 * @author auto
 * @since 2024-09-16
 */
public interface SkillObjectManageMapper {

  /**
   * 新增对象
   *
   * @param object 对象
   * @return 结果
   */
  int insertSkillObject(@Param("dto") SkillObjectDTO object);

  /**
   * 修改对象
   *
   * @param object 对象
   * @return 结果
   */
  int updateSkillObject(@Param("dto") SkillObjectDTO object);

  /**
   * 根据主键获取对象
   */
  SkillObjectDTO findSkillObject(@Param("tenantId") Long tenantId, @Param("id") Long busiObjectId);

  /**
   * 获取对象列表
   *
   * @param queryParams 查询条件
   * @return 对象列表
   */
  List<SkillObjectDTO> selectSkillObjectList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取对象列表（分页）
   *
   * @param queryParams 查询条件
   * @return 对象分页列表
   */
  Page<SkillObjectDTO> selectSkillObjectPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 删除对象
   */
  int deleteSkillObject(@Param("tenantId") Long tenantId, @Param("busiObjectId") Long busiObjectId, @Param("updatorId") Long updatorId);

  /**
   * 检验对象编码唯一性
   *
   * @param object 对象
   * @return 结果
   */
  boolean existsSkillObjectCode(@Param("dto") SkillObjectDTO object);
}
