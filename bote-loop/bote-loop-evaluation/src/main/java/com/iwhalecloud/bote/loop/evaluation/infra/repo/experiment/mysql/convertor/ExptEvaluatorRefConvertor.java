package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptEvaluatorRefEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorRef;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 实验评估器引用转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt_evaluator_ref.go
 * - 功能: 实验评估器引用DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: 列表转换
 * <p>
 * Java实现说明:
 * - 对应Go的ExptEvaluatorRefConverter结构体
 * - 处理列表转换
 * - 支持批量转换
 * <p>
 * 技术栈迁移:
 * - Go func DO2PO -> Java convertToPO
 * - Go func PO2DO -> Java convertToDO
 * - Go []*entity.ExptEvaluatorRef -> Java List<ExptEvaluatorRef>
 * - Go []*model.ExptEvaluatorRef -> Java List<ExptEvaluatorRefEntity>
 */
public final class ExptEvaluatorRefConvertor {

  private ExptEvaluatorRefConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 将DO列表转换为PO列表
   * 迁移对应关系: Go语言DO2PO
   *
   * @param refs 领域对象列表
   * @return 持久化对象列表
   */
  public static List<ExptEvaluatorRefEntity> convertToPO(List<ExptEvaluatorRef> refs) {
    if (refs == null) {
      return new ArrayList<>();
    }

    List<ExptEvaluatorRefEntity> models = new ArrayList<>(refs.size());
    Date now = new Date();
    for (ExptEvaluatorRef ref : refs) {
      models.add(ExptEvaluatorRefEntity.builder()
        .id(ref.getId())                                    // ID: ref.ID
        .spaceId(ref.getSpaceId())                          // SpaceID: ref.SpaceID
        .exptId(ref.getExptId())                            // ExptID: ref.ExptID
        .evaluatorId(ref.getEvaluatorId())                  // EvaluatorID: ref.EvaluatorID
        .evaluatorVersionId(ref.getEvaluatorVersionId())    // EvaluatorVersionID: ref.EvaluatorVersionID
        .createdAt(now)                                     // CreatedAt: 当前时间
        .updatedAt(now)                                     // UpdatedAt: 当前时间
        .deletedAt(0L)                                      // DeletedAt: 0（未删除）
        .build());
    }
    return models;
  }

  /**
   * 将PO列表转换为DO列表
   * 迁移对应关系: Go语言PO2DO
   *
   * @param refs 持久化对象列表
   * @return 领域对象列表
   */
  public static List<ExptEvaluatorRef> convertToDO(List<ExptEvaluatorRefEntity> refs) {
    if (refs == null) {
      return new ArrayList<>();
    }

    List<ExptEvaluatorRef> entities = new ArrayList<>(refs.size());
    for (ExptEvaluatorRefEntity ref : refs) {
      entities.add(ExptEvaluatorRef.builder()
        .spaceId(ref.getSpaceId())                          // SpaceID: ref.SpaceID
        .exptId(ref.getExptId())                            // ExptID: ref.ExptID
        .evaluatorId(ref.getEvaluatorId())                  // EvaluatorID: ref.EvaluatorID
        .evaluatorVersionId(ref.getEvaluatorVersionId())    // EvaluatorVersionID: ref.EvaluatorVersionID
        .build());
    }
    return entities;
  }
}
