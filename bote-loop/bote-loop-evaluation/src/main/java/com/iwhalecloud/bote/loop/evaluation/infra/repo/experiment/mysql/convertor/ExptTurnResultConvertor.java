package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorResults;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import java.util.Date;
import java.util.List;

/**
 * 实验轮次结果转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt_result.go
 * - 功能: 实验轮次结果DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: 指针类型转换、字符串字节转换
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResultConvertor结构体
 * - 处理指针类型转换
 * - 支持评估器结果转换
 * <p>
 * 技术栈迁移:
 * - Go func PO2DO -> Java convertToDO
 * - Go func DO2PO -> Java convertToPO
 * - Go gptr.Of -> Java 包装类型赋值
 * - Go gptr.Indirect -> Java 包装类型取值
 * - Go conv.UnsafeStringToBytes -> Java String.getBytes()
 * - Go conv.UnsafeBytesToString -> Java new String(bytes)
 */
public final class ExptTurnResultConvertor {

  private ExptTurnResultConvertor() {
    // 工具类，禁止实例化
  }

  public static ExptTurnResult convertToDO(ExptTurnResultEntity trEntity) {
    return convertToDO(trEntity, null);
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言PO2DO
   *
   * @param trEntity 持久化对象
   * @param evaluatorResults 评估器结果
   * @return 领域对象
   */
  public static ExptTurnResult convertToDO(ExptTurnResultEntity trEntity, EvaluatorResults evaluatorResults) {
    if (trEntity == null) {
      return null;
    }

    return ExptTurnResult.builder()
      .id(trEntity.getId())                      // ID: tr.ID
      .spaceId(trEntity.getSpaceId())            // SpaceID: tr.SpaceID
      .exptId(trEntity.getExptId())              // ExptID: tr.ExptID
      .exptRunId(trEntity.getExptRunId())        // ExptRunID: tr.ExptRunID
      .itemId(trEntity.getItemId())              // ItemID: tr.ItemID
      .turnId(trEntity.getTurnId())              // TurnID: tr.TurnID
      .status(trEntity.getStatus())              // Status: tr.Status
      .traceId(trEntity.getTraceId())            // TraceID: tr.TraceID
      .targetResultId(trEntity.getTargetResultId()) // TargetResultID: tr.TargetResultID
      .logId(trEntity.getLogId())                // LogID: tr.LogID
      .errMsg(trEntity.getErrMsg()) // ErrMsg: conv.UnsafeBytesToString(gptr.Indirect(tr.ErrMsg))
      .evaluatorResults(evaluatorResults)        // EvaluatorResults: evaluatorResults
      .turnIdx(trEntity.getTurnIdx())            // TurnIdx: gptr.Indirect(tr.TurnIdx)
      .createdAt(trEntity.getCreatedAt())        // CreatedAt: tr.CreatedAt
      .build();
  }

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言DO2PO
   *
   * @param tr 领域对象
   * @return 持久化对象
   */
  public static ExptTurnResultEntity convertToPO(ExptTurnResult tr) {
    if (tr == null) {
      return null;
    }

    return ExptTurnResultEntity.builder()
      .id(tr.getId())                            // ID: tr.ID
      .spaceId(tr.getSpaceId())                  // SpaceID: tr.SpaceID
      .exptId(tr.getExptId())                    // ExptID: tr.ExptID
      .exptRunId(tr.getExptRunId())              // ExptRunID: tr.ExptRunID
      .itemId(tr.getItemId())                    // ItemID: tr.ItemID
      .turnId(tr.getTurnId())                    // TurnID: tr.TurnID
      .status(tr.getStatus())                    // Status: tr.Status
      .traceId(tr.getTraceId())                  // TraceID: tr.TraceID
      .targetResultId(tr.getTargetResultId())    // TargetResultID: tr.TargetResultID
      .logId(tr.getLogId())                      // LogID: tr.LogID
      .errMsg(tr.getErrMsg()) // ErrMsg: gptr.Of(conv.UnsafeStringToBytes(tr.ErrMsg))
      .turnIdx(tr.getTurnIdx())                  // TurnIdx: gptr.Of(tr.TurnIdx)
      .deletedAt(0L)
      .createdAt(new Date())
      .build();
  }

  public static List<ExptTurnResult> convertToDOList(List<ExptTurnResultEntity> trs) {
    return trs.stream().map(o -> convertToDO(o, null)).toList();
  }

  public static List<ExptTurnResultEntity> convertToPOList(List<ExptTurnResult> dos) {
    return dos.stream().map(ExptTurnResultConvertor::convertToPO).toList();
  }
}
