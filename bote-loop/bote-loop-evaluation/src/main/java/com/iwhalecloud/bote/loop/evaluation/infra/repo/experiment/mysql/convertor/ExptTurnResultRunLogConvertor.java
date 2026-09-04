package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorResults;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import java.util.List;

/**
 * 实验轮次结果运行日志转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/convert/expt_run_log.go
 * - 功能: 实验轮次结果运行日志DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: JSON序列化/反序列化、指针类型转换
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResultRunLogConvertor结构体
 * - 处理JSON字段转换
 * - 支持指针类型转换
 * <p>
 * 技术栈迁移:
 * - Go func DO2PO -> Java convertToPO
 * - Go func PO2DO -> Java convertToDO
 * - Go json.Marshal -> Java ObjectMapper.writeValueAsString
 * - Go json.Unmarshal -> Java ObjectMapper.readValue
 * - Go gptr.Of -> Java 包装类型赋值
 * - Go gptr.Indirect -> Java 包装类型取值
 */
public final class ExptTurnResultRunLogConvertor {

  private ExptTurnResultRunLogConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言DO2PO
   *
   * @param log 领域对象
   * @return 持久化对象
   * @throws BssException 转换失败时抛出
   */
  public static ExptTurnResultRunLogEntity convertToPO(ExptTurnResultRunLog log) {
    if (log == null) {
      return null;
    }

    // EvaluatorResultIds: JSON序列化
    String evalResIDs = null;
    if (log.getEvaluatorResultIds() != null) {
      evalResIDs = JsonUtil.toJsonString(log.getEvaluatorResultIds());
    }

    return ExptTurnResultRunLogEntity.builder()
      .id(log.getId())                         // ID: log.ID
      .spaceId(log.getSpaceId())               // SpaceID: log.SpaceID
      .exptId(log.getExptId())                 // ExptID: log.ExptID
      .exptRunId(log.getExptRunId())           // ExptRunID: log.ExptRunID
      .itemId(log.getItemId())                 // ItemID: log.ItemID
      .turnId(log.getTurnId())                 // TurnID: log.TurnID
      .status(log.getStatus() != null ? log.getStatus().getValue() : null) // Status: int32(log.Status)
      .logId(log.getLogId())                   // LogID: log.LogID
      .targetResultId(log.getTargetResultId()) // TargetResultID: log.TargetResultID
      .evaluatorResultIds(evalResIDs)          // EvaluatorResultIds: gptr.Of(evalResIDs)
      .errMsg(log.getErrMsg()) // ErrMsg: gptr.Of(conv.UnsafeStringToBytes(log.ErrMsg))
      .createdAt(new Date())
      .deletedAt(0L)
      .build();
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言PO2DO
   *
   * @param logEntity 持久化对象
   * @return 领域对象
   * @throws BssException 转换失败时抛出
   */
  public static ExptTurnResultRunLog convertToDO(ExptTurnResultRunLogEntity logEntity) {
    if (logEntity == null) {
      return null;
    }

    // EvaluatorResultIds: JSON反序列化
    EvaluatorResults evalResIDs = new EvaluatorResults();
    if (logEntity.getEvaluatorResultIds() != null && !logEntity.getEvaluatorResultIds().isBlank()) {
      String evalResIDsJson = logEntity.getEvaluatorResultIds();
      if (!evalResIDsJson.isEmpty()) {
        evalResIDs = JsonUtil.parseJson(evalResIDsJson, EvaluatorResults.class);
      }
    }

    return ExptTurnResultRunLog.builder()
      .id(logEntity.getId())                   // ID: log.ID
      .spaceId(logEntity.getSpaceId())         // SpaceID: log.SpaceID
      .exptId(logEntity.getExptId())           // ExptID: log.ExptID
      .exptRunId(logEntity.getExptRunId())     // ExptRunID: log.ExptRunID
      .itemId(logEntity.getItemId())           // ItemID: log.ItemID
      .turnId(logEntity.getTurnId())           // TurnID: log.TurnID
      .status(logEntity.getStatus() != null ? TurnRunState.fromValue(logEntity.getStatus()) : null) // Status: entity.TurnRunState(log.Status)
      .logId(logEntity.getLogId())             // LogID: log.LogID
      .targetResultId(logEntity.getTargetResultId()) // TargetResultID: log.TargetResultID
      .evaluatorResultIds(evalResIDs)          // EvaluatorResultIds: evalResIDs
      .errMsg(logEntity.getErrMsg()) // ErrMsg: conv.UnsafeBytesToString(gptr.Indirect(log.ErrMsg))
      .build();
  }

  public static List<ExptTurnResultRunLogEntity> convertToPOList(List<ExptTurnResultRunLog> logs) {
    return logs.stream().map(ExptTurnResultRunLogConvertor::convertToPO).toList();
  }

  public static List<ExptTurnResultRunLog> convertToDOList(List<ExptTurnResultRunLogEntity> logEntities) {
    return logEntities.stream().map(ExptTurnResultRunLogConvertor::convertToDO).toList();
  }
}
