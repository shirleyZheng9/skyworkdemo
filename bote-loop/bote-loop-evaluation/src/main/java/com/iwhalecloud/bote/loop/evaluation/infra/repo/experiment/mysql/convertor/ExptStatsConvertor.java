package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptStatsEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import java.util.Date;

/**
 * 实验统计转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt_stats.go
 * - 功能: 实验统计DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: 指针类型转换、字段名映射
 * <p>
 * Java实现说明:
 * - 对应Go的ExptStatsConverter结构体
 * - 处理字段名映射（DO和PO字段名不同）
 * - 支持指针类型转换
 * <p>
 * 技术栈迁移:
 * - Go func DO2PO -> Java convertToPO
 * - Go func PO2DO -> Java convertToDO
 * - Go gptr.Of -> Java 包装类型赋值
 * - Go gptr.Indirect -> Java 包装类型取值
 */
public final class ExptStatsConvertor {
  private ExptStatsConvertor() {
  }

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言DO2PO
   *
   * @param stats 领域对象
   * @return 持久化对象
   */
  public static ExptStatsEntity convertToPO(ExptStats stats) {
    if (stats == null) {
      return null;
    }

    return ExptStatsEntity.builder()
      .id(stats.getId())                         // ID: stats.ID
      .spaceId(stats.getSpaceId())               // SpaceID: stats.SpaceID
      .exptId(stats.getExptId())                 // ExptID: stats.ExptID
      .pendingCnt(stats.getPendingItemCnt() != null ? stats.getPendingItemCnt() : 0)     // PendingCnt: stats.PendingItemCnt
      .successCnt(stats.getSuccessItemCnt() != null ? stats.getSuccessItemCnt() : 0)     // SuccessCnt: stats.SuccessItemCnt
      .failCnt(stats.getFailItemCnt() != null ? stats.getFailItemCnt() : 0)           // FailCnt: stats.FailItemCnt
      .terminatedCnt(stats.getTerminatedItemCnt() != null ? stats.getTerminatedItemCnt() : 0) // TerminatedCnt: stats.TerminatedItemCnt
      .processingCnt(stats.getProcessingItemCnt() != null ? stats.getProcessingItemCnt() : 0) // ProcessingCnt: stats.ProcessingItemCnt
      .creditCost(stats.getCreditCost() != null ? stats.getCreditCost() : 0.0)         // CreditCost: stats.CreditCost
      .inputTokenCost(stats.getInputTokenCost()) // InputTokenCost: gptr.Of(stats.InputTokenCost)
      .outputTokenCost(stats.getOutputTokenCost()) // OutputTokenCost: gptr.Of(stats.OutputTokenCost)
      .createdAt(stats.getCreatedAt() != null ? stats.getCreatedAt() : new Date())           // CreatedAt: stats.CreatedAt
      .updatedAt(stats.getUpdatedAt() != null ? stats.getUpdatedAt() : new Date())           // UpdatedAt: stats.UpdatedAt
      .deletedAt(0L)                             // DeletedAt: 默认未删除（0表示未删除）
      .build();
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言PO2DO
   *
   * @param statsEntity 持久化对象
   * @return 领域对象
   */
  public static ExptStats convertToDO(ExptStatsEntity statsEntity) {
    if (statsEntity == null) {
      return null;
    }

    return ExptStats.builder()
      .id(statsEntity.getId())                   // ID: stats.ID
      .spaceId(statsEntity.getSpaceId())         // SpaceID: stats.SpaceID
      .exptId(statsEntity.getExptId())           // ExptID: stats.ExptID
      .pendingItemCnt(statsEntity.getPendingCnt()) // PendingItemCnt: stats.PendingCnt
      .successItemCnt(statsEntity.getSuccessCnt()) // SuccessItemCnt: stats.SuccessCnt
      .failItemCnt(statsEntity.getFailCnt())     // FailItemCnt: stats.FailCnt
      .terminatedItemCnt(statsEntity.getTerminatedCnt()) // TerminatedItemCnt: stats.TerminatedCnt
      .processingItemCnt(statsEntity.getProcessingCnt()) // ProcessingItemCnt: stats.ProcessingCnt
      .creditCost(statsEntity.getCreditCost())   // CreditCost: stats.CreditCost
      .inputTokenCost(statsEntity.getInputTokenCost()) // InputTokenCost: gptr.Indirect(stats.InputTokenCost)
      .outputTokenCost(statsEntity.getOutputTokenCost()) // OutputTokenCost: gptr.Indirect(stats.OutputTokenCost)
      .createdAt(statsEntity.getCreatedAt())     // CreatedAt: stats.CreatedAt
      .updatedAt(statsEntity.getUpdatedAt())     // UpdatedAt: stats.UpdatedAt
      .build();
  }
}
