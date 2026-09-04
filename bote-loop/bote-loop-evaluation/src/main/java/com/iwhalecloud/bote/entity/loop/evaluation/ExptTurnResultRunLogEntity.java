package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果运行日志持久化对象
 * 迁移对应关系: Go语言model.ExptTurnResultRunLog
 * - 功能: 实验轮次结果运行日志的数据模型
 * - 表名: expt_turn_result_run_log
 * - 字段定义:
 * * id: Long - id
 * * spaceId: Long - 空间 id
 * * exptId: Long - 实验 id
 * * exptRunId: Long - 实验运行 id
 * * itemId: Long - item_id
 * * turnId: Long - turn_id
 * * status: Integer - 状态
 * * traceId: Long - trace_id
 * * logId: String - 日志 id
 * * targetResultId: Long - target_result_id
 * * evaluatorResultIds: byte[] - evaluator_result_ids，json list 格式
 * * errMsg: byte[] - 错误信息
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.ExptTurnResultRunLog结构体
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
 * - Go *string -> Java String
 * - Go *[]byte -> Java byte[]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expt_turn_result_run_log")
public class ExptTurnResultRunLogEntity {

  /**
   * id
   * 迁移对应关系: Go语言ExptTurnResultRunLog.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间 id
   * 迁移对应关系: Go语言ExptTurnResultRunLog.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 实验 id
   * 迁移对应关系: Go语言ExptTurnResultRunLog.ExptID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long exptId;

  /**
   * 实验运行 id
   * 迁移对应关系: Go语言ExptTurnResultRunLog.ExptRunID
   * - 功能: 实验运行标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long exptRunId;

  /**
   * item_id
   * 迁移对应关系: Go语言ExptTurnResultRunLog.ItemID
   * - 功能: 数据项标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long itemId;

  /**
   * turn_id
   * 迁移对应关系: Go语言ExptTurnResultRunLog.TurnID
   * - 功能: 轮次标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long turnId;

  /**
   * 状态
   * 迁移对应关系: Go语言ExptTurnResultRunLog.Status
   * - 功能: 状态
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer status;

  /**
   * trace_id
   * 迁移对应关系: Go语言ExptTurnResultRunLog.TraceID
   * - 功能: 追踪标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long traceId;

  /**
   * 日志 id
   * 迁移对应关系: Go语言ExptTurnResultRunLog.LogID
   * - 功能: 日志标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String logId;

  /**
   * target_result_id
   * 迁移对应关系: Go语言ExptTurnResultRunLog.TargetResultID
   * - 功能: 目标结果标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long targetResultId;

  /**
   * evaluator_result_ids，json list 格式
   * 迁移对应关系: Go语言ExptTurnResultRunLog.EvaluatorResultIds
   * - 功能: 评估器结果标识列表
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: blob binary
   */
  private String evaluatorResultIds;

  /**
   * 错误信息
   * 迁移对应关系: Go语言ExptTurnResultRunLog.ErrMsg
   * - 功能: 错误信息
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: blob binary
   */
  private String errMsg;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptTurnResultRunLog.CreatedAt
   * - 功能: 创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言ExptTurnResultRunLog.UpdatedAt
   * - 功能: 更新时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言ExptTurnResultRunLog.DeletedAt
   * - 功能: 软删除时间
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;
}
