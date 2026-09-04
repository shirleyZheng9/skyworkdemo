package com.iwhalecloud.bote.loop.prompt.application.convertor;

import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugLogDTO;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugLog;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * 调试日志转换器
 * 迁移对应关系: Go语言modules/prompt/application/convertor.DebugLogConvertor
 * - 功能: 将调试日志领域对象(DO)与数据传输对象(DTO)之间进行转换
 * - 主要方法:
 * * batchDebugLogDO2DTO - 批量DebugLog DO转DTO
 * * debugLogDO2DTO - DebugLog DO转DTO
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的转换函数
 * - 使用Java静态方法实现转换逻辑
 * - 处理空值检查和类型转换
 * - 支持批量转换操作
 * - 处理时间戳转换
 * <p>
 * 技术栈迁移:
 * - Go函数 -> Java静态方法
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 * - Go nil检查 -> Java null检查
 * - Go time.UnixMilli -> Java LocalDateTime转换
 * - Go ptr.Of -> Java直接赋值
 */
public final class DebugLogConvertor {

  private DebugLogConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 批量DebugLog DO转DTO
   * 迁移对应关系: Go语言convertor.BatchDebugLogDO2DTO
   * - 功能: 批量将DebugLog领域对象转换为DTO
   * - 参数: DebugLog领域对象列表
   * - 返回: DebugLog DTO列表
   * - 处理逻辑:
   * * 空值检查: 如果输入为空或空列表，返回null
   * * 批量转换: 遍历每个DebugLog对象，调用单个转换方法
   * * 过滤空值: 跳过转换结果为null的对象
   * * 结果检查: 如果转换后列表为空，返回null
   */
  public static List<DebugLogDTO> batchDebugLogDO2DTO(List<DebugLog> debugLogs) {
    if (debugLogs == null || debugLogs.isEmpty()) {
      return null;
    }

    List<DebugLogDTO> debugLogDTOs = new ArrayList<>();
    for (DebugLog debugLog : debugLogs) {
      if (debugLog == null) {
        continue;
      }
      debugLogDTOs.add(debugLogDO2DTO(debugLog));
    }
    return debugLogDTOs;
  }

  /**
   * DebugLog DO转DTO
   * 迁移对应关系: Go语言convertor.DebugLogDO2DTO
   * - 功能: 将DebugLog领域对象转换为DTO
   * - 参数: DebugLog领域对象
   * - 返回: DebugLog DTO对象
   * - 处理逻辑:
   * * 空值检查: 如果输入为null，返回null
   * * 字段映射: 将DO的字段映射到DTO的对应字段
   * * 类型转换: 处理必要的类型转换
   * * 时间转换: 将LocalDateTime转换为时间戳
   * * 对象构建: 使用Builder模式构建DTO对象
   */
  public static DebugLogDTO debugLogDO2DTO(DebugLog debugLog) {
    if (debugLog == null) {
      return null;
    }

    return DebugLogDTO.builder()
      .id(debugLog.getId())
      .promptId(debugLog.getPromptId())
      .workspaceId(debugLog.getSpaceId())
      .promptKey(debugLog.getPromptKey())
      .version(debugLog.getVersion())
      .inputTokens(debugLog.getInputTokens())
      .outputTokens(debugLog.getOutputTokens())
      .costMs(debugLog.getCostMs())
      .statusCode(debugLog.getStatusCode())
      .debuggedBy(debugLog.getDebuggedBy())
      .debugId(debugLog.getDebugId())
      .debugStep(debugLog.getDebugStep())
      .startedAt(convertLocalDateTimeToTimestamp(debugLog.getStartedAt()))
      .endedAt(convertLocalDateTimeToTimestamp(debugLog.getEndedAt()))
      .build();
  }

  /**
   * 时间戳转LocalDateTime
   * 迁移对应关系: 对应Go的时间处理
   * - 功能: 将时间戳转换为LocalDateTime
   * - 参数: 时间戳(毫秒)
   * - 返回: LocalDateTime对象
   */
  private static Long convertLocalDateTimeToTimestamp(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.toEpochSecond(ZoneOffset.UTC) * 1000;
  }
}
