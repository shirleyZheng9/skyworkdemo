package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunLog;

/**
 * 实验运行日志转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt_run_log.go
 * - 功能: 实验运行日志DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: 指针类型转换
 * <p>
 * Java实现说明:
 * - 对应Go的ExptRunLogConvertor结构体
 * - 处理指针类型转换
 * - 支持空值处理
 * <p>
 * 技术栈迁移:
 * - Go func DO2PO -> Java convertToPO
 * - Go func PO2DO -> Java convertToDO
 * - Go gptr.Of -> Java 包装类型赋值
 * - Go gptr.Indirect -> Java 包装类型取值
 */
public final class ExptRunLogConvertor {

  private ExptRunLogConvertor() {
  }

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言DO2PO
   *
   * @param log 领域对象
   * @return 持久化对象
   */
  public static ExptRunLogEntity convertToPO(ExptRunLog log) {
    if (log == null) {
      return null;
    }

    return ExptRunLogEntity.builder()
      .id(log.getId())                           // ID: log.ID
      .spaceId(log.getSpaceId())                 // SpaceID: log.SpaceID
      .createdBy(log.getCreatedBy())             // CreatedBy: log.CreatedBy
      .exptId(log.getExptId())                   // ExptID: log.ExptID
      .exptRunId(log.getExptRunId())             // ExptRunID: log.ExptRunID
      .itemIds(log.getItemIds())                 // ItemIds: byte[] -> String
      .mode(log.getMode())                       // Mode: gptr.Of(log.Mode)
      .status(log.getStatus())                   // Status: gptr.Of(log.Status)
      .pendingCnt(log.getPendingCnt())           // PendingCnt: log.PendingCnt
      .successCnt(log.getSuccessCnt())           // SuccessCnt: log.SuccessCnt
      .failCnt(log.getFailCnt())                 // FailCnt: log.FailCnt
      .creditCost(log.getCreditCost())           // CreditCost: log.CreditCost
      .tokenCost(log.getTokenCost())             // TokenCost: gptr.Of(log.TokenCost)
      .statusMessage(log.getStatusMessage())     // StatusMessage: byte[] -> String
      .processingCnt(log.getProcessingCnt())     // ProcessingCnt: log.ProcessingCnt
      .terminatedCnt(log.getTerminatedCnt())     // TerminatedCnt: log.TerminatedCnt
      .createdAt(log.getCreatedAt())             // CreatedAt: log.CreatedAt
      .updatedAt(log.getUpdatedAt())             // UpdatedAt: log.UpdatedAt
      .build();
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言PO2DO
   *
   * @param logEntity 持久化对象
   * @return 领域对象
   */
  public static ExptRunLog convertToDO(ExptRunLogEntity logEntity) {
    if (logEntity == null) {
      return null;
    }

    return ExptRunLog.builder()
      .id(logEntity.getId())                     // ID: log.ID
      .spaceId(logEntity.getSpaceId())           // SpaceID: log.SpaceID
      .createdBy(logEntity.getCreatedBy())       // CreatedBy: log.CreatedBy
      .exptId(logEntity.getExptId())             // ExptID: log.ExptID
      .exptRunId(logEntity.getExptRunId())       // ExptRunID: log.ExptRunID
      .itemIds(logEntity.getItemIds())           // ItemIds: String -> byte[]
      .mode(logEntity.getMode())                 // Mode: gptr.Indirect(log.Mode)
      .status(logEntity.getStatus())             // Status: gptr.Indirect(log.Status)
      .pendingCnt(logEntity.getPendingCnt())     // PendingCnt: log.PendingCnt
      .successCnt(logEntity.getSuccessCnt())     // SuccessCnt: log.SuccessCnt
      .failCnt(logEntity.getFailCnt())           // FailCnt: log.FailCnt
      .creditCost(logEntity.getCreditCost())     // CreditCost: log.CreditCost
      .tokenCost(logEntity.getTokenCost())       // TokenCost: gptr.Indirect(log.TokenCost)
      .statusMessage(logEntity.getStatusMessage()) // StatusMessage: String -> byte[]
      .processingCnt(logEntity.getProcessingCnt()) // ProcessingCnt: log.ProcessingCnt
      .terminatedCnt(logEntity.getTerminatedCnt()) // TerminatedCnt: log.TerminatedCnt
      .createdAt(logEntity.getCreatedAt())       // CreatedAt: log.CreatedAt
      .updatedAt(logEntity.getUpdatedAt())       // UpdatedAt: log.UpdatedAt
      .build();
  }
}
