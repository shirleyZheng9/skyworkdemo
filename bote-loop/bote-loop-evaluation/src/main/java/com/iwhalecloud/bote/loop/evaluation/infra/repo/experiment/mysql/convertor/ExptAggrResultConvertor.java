package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptAggrResultEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggrResult;
import java.util.Date;

/**
 * 实验聚合结果转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/aggr_result.go
 * - 功能: 实验聚合结果DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: 指针类型的转换（gptr.Of和gptr.Indirect）
 * <p>
 * Java实现说明:
 * - 对应Go的ExptAggrResultDOToPO和ExptAggrResultPOToDO函数
 * - 处理Go语言指针类型到Java包装类型的转换
 * - 支持空值处理
 * <p>
 * 技术栈迁移:
 * - Go func ExptAggrResultDOToPO -> Java convertToPO
 * - Go func ExptAggrResultPOToDO -> Java convertToDO
 * - Go gptr.Of -> Java 包装类型赋值（非null时）
 * - Go gptr.Indirect -> Java 包装类型取值（非null时）
 * - Go *int32 -> Java Integer
 * - Go *float64 -> Java Double
 * - Go *[]byte -> Java byte[]
 */
public final class ExptAggrResultConvertor {

  private ExptAggrResultConvertor() {
  }

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言ExptAggrResultDOToPO
   *
   * @param doEntity 领域对象
   * @return 持久化对象
   */
  public static ExptAggrResultEntity convertToPO(ExptAggrResult doEntity) {
    if (doEntity == null) {
      return null;
    }

    return ExptAggrResultEntity.builder()
      .id(doEntity.getId())                    // ID: do.ID
      .spaceId(doEntity.getSpaceId())          // SpaceID: do.SpaceID
      .experimentId(doEntity.getExperimentId()) // ExperimentID: do.ExperimentID
      .fieldType(doEntity.getFieldType())      // FieldType: gptr.Of(do.FieldType)
      .fieldKey(doEntity.getFieldKey())        // FieldKey: do.FieldKey
      .score(doEntity.getScore())              // Score: gptr.Of(do.Score)
      .aggrResult(doEntity.getAggrResult())    // AggrResult: byte[] -> String
      .version(doEntity.getVersion())          // Version: do.Version
      .status(doEntity.getStatus())            // Status: do.Status
      .createdAt(new Date())
      .deletedAt(0L)
      .build();
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言ExptAggrResultPOToDO
   *
   * @param poEntity 持久化对象
   * @return 领域对象
   */
  public static ExptAggrResult convertToDO(ExptAggrResultEntity poEntity) {
    if (poEntity == null) {
      return null;
    }

    return ExptAggrResult.builder()
      .id(poEntity.getId())                    // ID: po.ID
      .spaceId(poEntity.getSpaceId())          // SpaceID: po.SpaceID
      .experimentId(poEntity.getExperimentId()) // ExperimentID: po.ExperimentID
      .fieldType(poEntity.getFieldType())      // FieldType: gptr.Indirect(po.FieldType)
      .fieldKey(poEntity.getFieldKey())        // FieldKey: po.FieldKey
      .score(poEntity.getScore())              // Score: gptr.Indirect(po.Score)
      .aggrResult(poEntity.getAggrResult())    // AggrResult: String -> byte[]
      .version(poEntity.getVersion())          // Version: po.Version
      .status(poEntity.getStatus())            // Status: po.Status
      .build();
  }
}
