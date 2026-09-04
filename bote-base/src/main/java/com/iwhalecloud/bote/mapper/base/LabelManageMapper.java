package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.LabelDTO;
import com.iwhalecloud.bote.dto.base.query.LabelQueryParams;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 标签管理
 *
 * @author auto
 * @since 2024-09-13
 */
public interface LabelManageMapper {
  /**
   * 校验标签的唯一性
   *
   * @param label 标签
   * @return 结果
   */
  boolean existsLabelName(@Param("dto") LabelDTO label);

  /**
   * 根据主键获取标签
   *
   * @param labelId 标签主键
   * @return 标签
   */
  LabelDTO getLabel(@Param("id") Long labelId, @Param("tenantId") Long tenantId);

  /**
   * 新增标签
   *
   * @param label 标签
   * @return 结果
   */
  int insertLabel(@Param("dto") LabelDTO label);

  /**
   * 修改标签
   *
   * @param label 标签
   * @return 结果
   */
  int updateLabel(@Param("dto") LabelDTO label);

  /**
   * 删除属性
   *
   * @param labelId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteLabel(@Param("labelId") Long labelId, @Param("updatorId") Long updatorId, @Param("tenantId") Long tenantId);

  /**
   * 获取标签列表
   *
   * @param queryParams 查询条件
   * @return 标签列表
   */
  List<LabelDTO> selectLabelList(@Param("query") LabelQueryParams queryParams);

  /**
   * 批量新增关联标签
   *
   * @param labelObjectRels 关联标签列表
   * @return 结果
   */
  int batchInsertLabelObjectRel(@Param("list") List<LabelObjectRelDTO> labelObjectRels);

  /**
   * 修改关联标签
   *
   * @param labelObjectRel 关联标签
   * @return 结果
   */
  int updateLabelObjectRel(@Param("dto") LabelObjectRelDTO labelObjectRel);

  /**
   * 删除关联标签
   *
   * @param relIds 主键集合
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteLabelObjectRel(@Param("relIds") List<Long> relIds, @Param("updatorId") Long updatorId, @Param("tenantId") Long tenantId);

  /**
   * 根据对象，查询关联的标签
   *
   * @param objectIds 对象 ID 集合
   * @param objectType 对象名称
   * @param tenantId 租户 ID
   * @return 关联标签
   */
  List<LabelObjectRelDTO> selectLabelObjectRelList(@Param("objectIds") List<Long> objectIds, @Param("objectType") String objectType,
    @Param("tenantId") Long tenantId);

  /**
   * 根据标签，查询关联的对象
   *
   * @param labelIds 标签 ID 集合
   * @param labelType 标签类型
   * @param tenantId 租户 ID
   * @return 关联标签
   */
  List<LabelObjectRelDTO> selectLabelRelList(@Param("labelIds") List<Long> labelIds, @Param("labelType") String labelType,
    @Param("tenantId") Long tenantId);
}
