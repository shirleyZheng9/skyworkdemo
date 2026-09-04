package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试日志实体
 * 迁移对应关系: Go语言entity.DebugLog
 * - 功能: 调试日志的数据结构
 * - 字段定义:
 * * ID: int64 - 日志ID
 * * PromptID: int64 - Prompt ID
 * * SpaceID: int64 - 空间ID
 * * PromptKey: string - Prompt键
 * * Version: string - 版本
 * * InputTokens: int64 - 输入Token数
 * * OutputTokens: int64 - 输出Token数
 * * StartedAt: time.Time - 开始时间
 * * EndedAt: time.Time - 结束时间
 * * CostMS: int64 - 耗时(毫秒)
 * * StatusCode: int32 - 状态码
 * * DebuggedBy: string - 调试者
 * * DebugID: int64 - 调试ID
 * * DebugStep: int32 - 调试步骤
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DebugLog结构体
 * - 使用Java类定义，包含所有调试日志字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 时间字段使用LocalDateTime
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go time.Time -> Java LocalDateTime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugLog {

  /**
   * 日志ID
   * 迁移对应关系: Go语言entity.DebugLog.ID (int64)
   * - 功能: 调试日志的唯一标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 数据库主键和业务标识
   */
  @JsonProperty("id")
  private Long id;

  /**
   * Prompt ID
   * 迁移对应关系: Go语言entity.DebugLog.PromptID (int64)
   * - 功能: 关联的Prompt标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 关联Prompt和调试日志
   */
  @JsonProperty("prompt_id")
  private Long promptId;

  /**
   * 空间ID
   * 迁移对应关系: Go语言entity.DebugLog.SpaceID (int64)
   * - 功能: 所属空间的标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 空间隔离和权限控制
   */
  @JsonProperty("space_id")
  private Long spaceId;

  /**
   * Prompt键
   * 迁移对应关系: Go语言entity.DebugLog.PromptKey (string)
   * - 功能: Prompt的业务键
   * - 类型: Go的string对应Java的String
   * - 用途: 业务标识和查询优化
   */
  @JsonProperty("prompt_key")
  private String promptKey;

  /**
   * 版本
   * 迁移对应关系: Go语言entity.DebugLog.Version (string)
   * - 功能: Prompt的版本号
   * - 类型: Go的string对应Java的String
   * - 用途: 版本管理和历史追踪
   */
  @JsonProperty("version")
  private String version;

  /**
   * 输入Token数
   * 迁移对应关系: Go语言entity.DebugLog.InputTokens (int64)
   * - 功能: 输入内容的Token数量
   * - 类型: Go的int64对应Java的Long
   * - 用途: 成本计算和性能分析
   */
  @JsonProperty("input_tokens")
  private Long inputTokens;

  /**
   * 输出Token数
   * 迁移对应关系: Go语言entity.DebugLog.OutputTokens (int64)
   * - 功能: 输出内容的Token数量
   * - 类型: Go的int64对应Java的Long
   * - 用途: 成本计算和性能分析
   */
  @JsonProperty("output_tokens")
  private Long outputTokens;

  /**
   * 开始时间
   * 迁移对应关系: Go语言entity.DebugLog.StartedAt (time.Time)
   * - 功能: 调试开始的时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 用途: 时间追踪和性能分析
   */
  @JsonProperty("started_at")
  private LocalDateTime startedAt;

  /**
   * 结束时间
   * 迁移对应关系: Go语言entity.DebugLog.EndedAt (time.Time)
   * - 功能: 调试结束的时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 用途: 时间追踪和性能分析
   */
  @JsonProperty("ended_at")
  private LocalDateTime endedAt;

  /**
   * 耗时(毫秒)
   * 迁移对应关系: Go语言entity.DebugLog.CostMS (int64)
   * - 功能: 调试总耗时
   * - 类型: Go的int64对应Java的Long
   * - 用途: 性能分析和监控
   */
  @JsonProperty("cost_ms")
  private Long costMs;

  /**
   * 状态码
   * 迁移对应关系: Go语言entity.DebugLog.StatusCode (int32)
   * - 功能: 调试执行的状态码
   * - 类型: Go的int32对应Java的Integer
   * - 用途: 错误追踪和状态管理
   */
  @JsonProperty("status_code")
  private Integer statusCode;

  /**
   * 调试者
   * 迁移对应关系: Go语言entity.DebugLog.DebuggedBy (string)
   * - 功能: 执行调试的用户标识
   * - 类型: Go的string对应Java的String
   * - 用途: 用户追踪和权限控制
   */
  @JsonProperty("debugged_by")
  private String debuggedBy;

  /**
   * 调试ID
   * 迁移对应关系: Go语言entity.DebugLog.DebugID (int64)
   * - 功能: 调试会话的唯一标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 调试会话管理和追踪
   */
  @JsonProperty("debug_id")
  private Long debugId;

  /**
   * 调试步骤
   * 迁移对应关系: Go语言entity.DebugLog.DebugStep (int32)
   * - 功能: 调试的步骤编号
   * - 类型: Go的int32对应Java的Integer
   * - 用途: 步骤追踪和调试管理
   */
  @JsonProperty("debug_step")
  private Integer debugStep;

}
