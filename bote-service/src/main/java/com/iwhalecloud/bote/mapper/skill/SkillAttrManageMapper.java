package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrSpecDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrValueDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrValueRelDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：属性管理
 *
 * @author auto
 * @since 2024-09-15
 */
public interface SkillAttrManageMapper {

  /**
   * 校验属性编码唯一性
   *
   * @param attrSpec 属性
   * @return 结果
   */
  boolean checkAttrNbr(@Param("dto") SkillAttrSpecDTO attrSpec);

  /**
   * 根据主键获取属性
   */
  SkillAttrSpecDTO getAttrSpec(@Param("tenantId") Long tenantId, @Param("id") Long attrId);

  /**
   * 根据编码获取属性
   *
   * @param attrCode 属性编码
   * @param tenantId 租户 ID
   * @return AttrSpec
   */
  SkillAttrSpecDTO getAttrSpecByCode(@Param("attrCode") String attrCode, @Param("tenantId") Long tenantId);

  /**
   * 新增属性
   *
   * @param attrSpec 属性
   * @return 结果
   */
  int insertAttrSpec(@Param("dto") SkillAttrSpecDTO attrSpec);

  /**
   * 批量新增属性
   *
   * @param list 属性列表
   * @return 批量结果
   */
  int batchInsertAttrSpec(@Param("list") List<SkillAttrSpecDTO> list);

  /**
   * 修改属性
   *
   * @param attrSpec 属性
   * @return 结果
   */
  int updateAttrSpec(@Param("dto") SkillAttrSpecDTO attrSpec);

  /**
   * 删除属性
   */
  int deleteAttrSpec(@Param("tenantId") Long tenantId, @Param("attrId") Long attrId, @Param("updatorId") Long updatorId);

  /**
   * 获取属性列表
   *
   * @param queryParams 查询条件
   * @return AttrSpecEntity列表
   */
  List<SkillAttrSpecDTO> selectAttrSpecList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取属性列表（分页）
   *
   * @param queryParams 查询条件
   * @return 属性分页列表
   */
  Page<SkillAttrSpecDTO> selectAttrSpecPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 批量新增属性值
   *
   * @param attrValues 属性值列表
   * @return 结果
   */
  int batchInsertAttrValue(@Param("list") List<SkillAttrValueDTO> attrValues);

  /**
   * 批量新增属性值关联
   *
   * @param attrValueRelList 属性值关联列表
   * @return 结果
   */
  int batchInsertAttrValueRel(@Param("list") List<SkillAttrValueRelDTO> attrValueRelList);

  /**
   * 修改属性值关联
   *
   * @param attrValueRel 属性值关联
   * @return 结果
   */
  int updateAttrValueRel(@Param("dto") SkillAttrValueRelDTO attrValueRel);

  /**
   * 修改属性值
   *
   * @param attrValue 属性值
   * @return 结果
   */
  int updateAttrValue(@Param("dto") SkillAttrValueDTO attrValue);

  /**
   * 获取属性值列表
   */
  List<SkillAttrValueDTO> selectAttrValueList(@Param("tenantId") Long tenantId, @Param("attrId") Long attrId);

  /**
   * 根据静态编码查询静态值列表
   *
   * @param tenantId 租户 ID
   * @param attrCode 静态编码
   * @return 静态值列表
   */
  List<SimpleAttrDTO> selectAttrValueByAttrCode(@Param("tenantId") Long tenantId, @Param("attrCode") String attrCode);

  /**
   * 查询所有静态值列表
   *
   * @return 静态值列表
   */
  List<SimpleAttrDTO> selectAllAttrValue(@Param("tenantId") Long tenantId);

  /**
   * 批量查询关联属性列表
   */
  List<SkillAttrValueRelDTO> selectAttrValueRelList(@Param("tenantId") Long tenantId, @Param("attrValueIds") List<Long> attrValueIds);

  /**
   * 批量删除属性值关联
   *
   * @param tenantId 租户 ID
   * @param zAttId 属性 ID
   * @param updatorId 修改人 ID
   * @return 删除结果
   */
  int deleteAttrValueRel(@Param("tenantId") Long tenantId, @Param("zAttId") Long zAttId, @Param("updatorId") Long updatorId);
}
