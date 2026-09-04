package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnEvaluatorResultRefEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import java.util.List;

/**
 * 实验轮次评估器结果引用转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt_turn_evaluator_result_ref.go
 * - 功能: 实验轮次评估器结果引用DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: 直接字段映射
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnEvaluatorResultRefConvertor结构体
 * - 处理直接字段映射
 * - 无特殊转换逻辑
 * <p>
 * 技术栈迁移:
 * - Go func DO2PO -> Java convertToPO
 * - Go func PO2DO -> Java convertToDO
 * - Go 直接字段映射 -> Java 直接字段映射
 */
public final class ExptTurnEvaluatorResultRefConvertor {

  private ExptTurnEvaluatorResultRefConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言DO2PO
   *
   * @param ref 领域对象
   * @return 持久化对象
   */
  public static ExptTurnEvaluatorResultRefEntity convertToPO(ExptTurnEvaluatorResultRef ref) {
    if (ref == null) {
      return null;
    }

    return ExptTurnEvaluatorResultRefEntity.builder()
      .id(ref.getId())                           // ID: ref.ID
      .spaceId(ref.getSpaceId())                 // SpaceID: ref.SpaceID
      .exptTurnResultId(ref.getExptTurnResultId()) // ExptTurnResultID: ref.ExptTurnResultID
      .evaluatorVersionId(ref.getEvaluatorVersionId()) // EvaluatorVersionID: ref.EvaluatorVersionID
      .evaluatorResultId(ref.getEvaluatorResultId()) // EvaluatorResultID: ref.EvaluatorResultID
      .exptId(ref.getExptId())                   // ExptID: ref.ExptID
      .deletedAt(0L)
      .build();
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言PO2DO
   *
   * @param refEntity 持久化对象
   * @return 领域对象
   */
  public static ExptTurnEvaluatorResultRef convertToDO(ExptTurnEvaluatorResultRefEntity refEntity) {
    if (refEntity == null) {
      return null;
    }

    return ExptTurnEvaluatorResultRef.builder()
      .id(refEntity.getId())                     // ID: ref.ID
      .spaceId(refEntity.getSpaceId())           // SpaceID: ref.SpaceID
      .exptTurnResultId(refEntity.getExptTurnResultId()) // ExptTurnResultID: ref.ExptTurnResultID
      .evaluatorVersionId(refEntity.getEvaluatorVersionId()) // EvaluatorVersionID: ref.EvaluatorVersionID
      .evaluatorResultId(refEntity.getEvaluatorResultId()) // EvaluatorResultID: ref.EvaluatorResultID
      .exptId(refEntity.getExptId())             // ExptID: ref.ExptID
      .build();
  }

  public static List<ExptTurnEvaluatorResultRef> convertToDOList(List<ExptTurnEvaluatorResultRefEntity> refEntities) {
    return refEntities.stream().map(ExptTurnEvaluatorResultRefConvertor::convertToDO).toList();
  }

  public static List<ExptTurnEvaluatorResultRefEntity> convertToPOList(List<ExptTurnEvaluatorResultRef> dos) {
    return dos.stream().map(ExptTurnEvaluatorResultRefConvertor::convertToPO).toList();
  }
}
