package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterKeyMappingEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterKeyMapping;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldTypeMapping;
import java.util.Date;
import java.util.List;

/**
 * 实验轮次结果过滤键映射转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt_turn_result_filter_key_mapping.go
 * - 功能: 实验轮次结果过滤键映射DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: 枚举类型转换
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResultFilterKeyMappingDO2PO和ExptTurnResultFilterKeyMappingPO2DO函数
 * - 处理枚举类型转换
 * - 支持空值处理
 * <p>
 * 技术栈迁移:
 * - Go func ExptTurnResultFilterKeyMappingDO2PO -> Java convertToPO
 * - Go func ExptTurnResultFilterKeyMappingPO2DO -> Java convertToDO
 * - Go int32(do.FieldType) -> Java fieldType.getValue()
 * - Go entity.FieldTypeMapping(po.FieldType) -> Java FieldTypeMapping.fromValue(fieldType)
 */
public final class ExptTurnResultFilterKeyMappingConvertor {

  private ExptTurnResultFilterKeyMappingConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMappingDO2PO
   *
   * @param doEntity 领域对象
   * @return 持久化对象
   */
  public static ExptTurnResultFilterKeyMappingEntity convertToPO(ExptTurnResultFilterKeyMapping doEntity) {
    if (doEntity == null) {
      return null;
    }

    return ExptTurnResultFilterKeyMappingEntity.builder()
      .id(doEntity.getId())                      // ID: do.ID
      .spaceId(doEntity.getSpaceId())            // SpaceID: do.SpaceID
      .exptId(doEntity.getExptId())              // ExptID: do.ExptID
      .fromField(doEntity.getFromField())        // FromField: do.FromField
      .toKey(doEntity.getToKey())                // ToKey: do.ToKey
      .fieldType(doEntity.getFieldType() != null ? doEntity.getFieldType().getValue() : null) // FieldType: int32(do.FieldType)
      .createdAt(new Date())                     // CreatedAt: 设置创建时间
      .deletedAt(null)                           // DeletedAt: 默认为null
      .createdBy(null)                           // CreatedBy: 默认为null
      .build();
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMappingPO2DO
   *
   * @param poEntity 持久化对象
   * @return 领域对象
   */
  public static ExptTurnResultFilterKeyMapping convertToDO(ExptTurnResultFilterKeyMappingEntity poEntity) {
    if (poEntity == null) {
      return null;
    }

    return ExptTurnResultFilterKeyMapping.builder()
      .id(poEntity.getId())                      // ID: po.ID
      .spaceId(poEntity.getSpaceId())            // SpaceID: po.SpaceID
      .exptId(poEntity.getExptId())              // ExptID: po.ExptID
      .fromField(poEntity.getFromField())        // FromField: po.FromField
      .toKey(poEntity.getToKey())                // ToKey: po.ToKey
      .fieldType(poEntity.getFieldType() != null ? FieldTypeMapping.fromValue(poEntity.getFieldType()) : null) // FieldType: entity.FieldTypeMapping(po.FieldType)
      .build();
  }

  public static List<ExptTurnResultFilterKeyMapping> convertToDOList(List<ExptTurnResultFilterKeyMappingEntity> pos) {
    return pos.stream().map(ExptTurnResultFilterKeyMappingConvertor::convertToDO).toList();
  }

  public static List<ExptTurnResultFilterKeyMappingEntity> convertToPOList(List<ExptTurnResultFilterKeyMapping> dos) {
    return dos.stream().map(ExptTurnResultFilterKeyMappingConvertor::convertToPO).toList();
  }
}
