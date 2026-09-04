package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.base.LabelDTO;
import com.iwhalecloud.bote.dto.base.query.LabelQueryParams;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import java.util.List;

/**
 * 标签管理服务
 *
 * @author chen.linfa
 * @since 2024-09-11
 */
public interface ILabelManageService {
  /**
   * 保存标签
   *
   * @param label 标签
   * @return 结果
   */
  ResultVO<LabelDTO> saveLabel(LabelDTO label);

  /**
   * 删除标签
   *
   * @param tenantId 租户 ID
   * @param labelId 标签主键
   * @return 结果
   */
  ResultVO<Void> deleteLabel(Long tenantId, Long labelId);

  /**
   * 按照分类查询标签列表
   *
   * @param queryParams 查询条件
   * @return 标签列表
   */
  List<LabelDTO> queryLabelList(LabelQueryParams queryParams);

  /**
   * 根据对象查询关联标签
   *
   * @param objectIds 对象 ID
   * @param objectType 对象类型
   * @param tenantId 租户 ID
   * @return 关联标签
   */
  List<LabelObjectRelDTO> queryLabelObjectRelList(List<Long> objectIds, String objectType, Long tenantId);

  /**
   * 保存关联标签
   *
   * @param labelObjectRels 关联标签
   * @return 结果
   */
  ResultVO<Void> saveLabelObjectRel(List<LabelObjectRelDTO> labelObjectRels);

  /**
   * 删除关联标签
   *
   * @param relIds 主键集合
   * @param tenantId 租户 ID
   * @return 结果
   */
  ResultVO<Void> deleteLabelObjectRel(List<Long> relIds, Long tenantId);
}
