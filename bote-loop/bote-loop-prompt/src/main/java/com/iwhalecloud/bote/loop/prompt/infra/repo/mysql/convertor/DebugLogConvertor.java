package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.convertor;

import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugLogEntity;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugLog;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * 调试日志转换器
 * 迁移对应关系: Go语言modules/prompt/infra/repo/mysql/convertor.DebugLogConvertor
 * - 功能: 将调试日志领域对象(DO)与持久化对象(PO)之间进行转换
 * - 主要方法:
 * * debugLogsPO2DO - 批量DebugLog PO转DO
 * * debugLogPO2DO - DebugLog PO转DO
 * * debugLogDO2PO - DebugLog DO转PO
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的转换函数
 * - 使用Java静态方法实现转换逻辑
 * - 处理空值检查和类型转换
 * - 支持批量转换操作
 * <p>
 * 技术栈迁移:
 * - Go函数 -> Java静态方法
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 * - Go nil检查 -> Java null检查
 * - Go time.Time -> Java LocalDateTime
 * - Go int64 -> Java Long
 */
public final class DebugLogConvertor {

  private DebugLogConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 批量转换DebugLog PO到DO
   * 迁移对应关系: Go语言convertor.DebugLogsPO2DO
   * - 功能: 将调试日志PO列表转换为DO列表
   * - 参数: 调试日志PO列表
   * - 返回: 调试日志DO列表
   */
  public static List<DebugLog> debugLogsPO2DO(List<PromptDebugLogEntity> pos) {
    if (pos == null) {
      return null;
    }

    List<DebugLog> debugLogs = new ArrayList<>();
    for (PromptDebugLogEntity po : pos) {
      if (po == null) {
        continue;
      }
      debugLogs.add(debugLogPO2DO(po));
    }

    return debugLogs.isEmpty() ? null : debugLogs;
  }

  /**
   * 转换DebugLog PO到DO
   * 迁移对应关系: Go语言convertor.DebugLogPO2DO
   * - 功能: 将调试日志PO转换为DO
   * - 参数: 调试日志PO
   * - 返回: 调试日志DO
   */
  public static DebugLog debugLogPO2DO(PromptDebugLogEntity po) {
    if (po == null) {
      return null;
    }

    return DebugLog.builder()
      .id(po.getId())
      .promptId(po.getPromptId())
      .spaceId(po.getSpaceId())
      .promptKey(po.getPromptKey())
      .version(po.getVersion())
      .inputTokens(po.getInputTokens())
      .outputTokens(po.getOutputTokens())
      .startedAt(po.getStartedAt() != null ?
        LocalDateTime.ofInstant(Instant.ofEpochMilli(po.getStartedAt()), ZoneOffset.UTC) : null)
      .endedAt(po.getEndedAt() != null ?
        LocalDateTime.ofInstant(Instant.ofEpochMilli(po.getEndedAt()), ZoneOffset.UTC) : null)
      .costMs(po.getCostMs())
      .statusCode(po.getStatusCode())
      .debuggedBy(po.getDebuggedBy())
      .debugId(po.getDebugId())
      .debugStep(po.getDebugStep())
      .build();
  }

  /**
   * 转换DebugLog DO到PO
   * 迁移对应关系: Go语言convertor.DebugLogDO2PO
   * - 功能: 将调试日志DO转换为PO
   * - 参数: 调试日志DO
   * - 返回: 调试日志PO
   */
  public static PromptDebugLogEntity debugLogDO2PO(DebugLog debugLog) {
    if (debugLog == null) {
      return null;
    }

    return PromptDebugLogEntity.builder()
      .id(debugLog.getId())
      .promptId(debugLog.getPromptId())
      .spaceId(debugLog.getSpaceId())
      .promptKey(debugLog.getPromptKey())
      .version(debugLog.getVersion())
      .inputTokens(debugLog.getInputTokens())
      .outputTokens(debugLog.getOutputTokens())
      .startedAt(debugLog.getStartedAt() != null ?
        debugLog.getStartedAt().toInstant(ZoneOffset.UTC).toEpochMilli() : null)
      .endedAt(debugLog.getEndedAt() != null ?
        debugLog.getEndedAt().toInstant(ZoneOffset.UTC).toEpochMilli() : null)
      .costMs(debugLog.getCostMs())
      .statusCode(debugLog.getStatusCode())
      .debuggedBy(debugLog.getDebuggedBy())
      .debugId(debugLog.getDebugId())
      .debugStep(debugLog.getDebugStep())
      .build();
  }
}
