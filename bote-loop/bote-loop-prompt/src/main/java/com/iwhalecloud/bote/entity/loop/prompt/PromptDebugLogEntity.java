package com.iwhalecloud.bote.entity.loop.prompt;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt调试日志持久化对象
 * 迁移对应关系: Go语言model.PromptDebugLog
 * - 功能: debug表的数据模型
 * - 表名: prompt_debug_log
 * - 字段定义:
 * * id: Long - 主键ID
 * * promptId: Long - Prompt ID
 * * spaceId: Long - 空间ID
 * * promptKey: String - prompt key
 * * version: String - version
 * * inputTokens: Long - input_tokens
 * * outputTokens: Long - output_tokens
 * * startedAt: Long - 请求开始毫秒时间戳
 * * endedAt: Long - 响应结束毫秒时间戳
 * * costMs: Long - 响应耗时毫秒
 * * statusCode: Integer - 状态码
 * * debuggedBy: String - 执行人UserID
 * * debugId: Long - debug_id
 * * debugStep: Integer - debug_step
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.PromptDebugLog结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 支持软删除功能
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go soft_delete.DeletedAt -> Java Long
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go int32 -> Java Integer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bt_prompt_debug_log")
public class PromptDebugLogEntity {

  /**
   * 主键ID
   * 迁移对应关系: Go语言PromptDebugLog.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * Prompt ID
   * 迁移对应关系: Go语言PromptDebugLog.PromptID
   * - 功能: Prompt标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long promptId;

  /**
   * 空间ID
   * 迁移对应关系: Go语言PromptDebugLog.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * prompt key
   * 迁移对应关系: Go语言PromptDebugLog.PromptKey
   * - 功能: Prompt键值
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String promptKey;

  /**
   * version
   * 迁移对应关系: Go语言PromptDebugLog.Version
   * - 功能: 版本号
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String version;

  /**
   * input_tokens
   * 迁移对应关系: Go语言PromptDebugLog.InputTokens
   * - 功能: 输入令牌数
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20), not null
   */
  private Long inputTokens;

  /**
   * output_tokens
   * 迁移对应关系: Go语言PromptDebugLog.OutputTokens
   * - 功能: 输出令牌数
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20), not null
   */
  private Long outputTokens;

  /**
   * 请求开始毫秒时间戳
   * 迁移对应关系: Go语言PromptDebugLog.StartedAt
   * - 功能: 请求开始时间
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20) unsigned
   */
  private Long startedAt;

  /**
   * 响应结束毫秒时间戳
   * 迁移对应关系: Go语言PromptDebugLog.EndedAt
   * - 功能: 响应结束时间
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20) unsigned
   */
  private Long endedAt;

  /**
   * 响应耗时毫秒
   * 迁移对应关系: Go语言PromptDebugLog.CostMs
   * - 功能: 响应耗时
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20) unsigned
   */
  private Long costMs;

  /**
   * 状态码
   * 迁移对应关系: Go语言PromptDebugLog.StatusCode
   * - 功能: HTTP状态码
   * - 类型: Go的*int32对应Java的Integer
   * - 数据库: int(11)
   */
  private Integer statusCode;

  /**
   * 执行人UserID
   * 迁移对应关系: Go语言PromptDebugLog.DebuggedBy
   * - 功能: 调试执行人
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(128), default 0
   */
  private String debuggedBy;

  /**
   * debug_id
   * 迁移对应关系: Go语言PromptDebugLog.DebugID
   * - 功能: 调试会话ID
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long debugId;

  /**
   * debug_step
   * 迁移对应关系: Go语言PromptDebugLog.DebugStep
   * - 功能: 调试步骤
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null, default 1
   */
  private Integer debugStep;

  /**
   * 创建时间
   * 迁移对应关系: Go语言PromptDebugLog.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言PromptDebugLog.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言PromptDebugLog.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: bigint(20), not null, default 0
   */
  private Long deletedAt;
}
