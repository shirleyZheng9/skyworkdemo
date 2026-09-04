package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验运行日志持久化对象
 * 迁移对应关系: Go语言model.ExptRunLog
 * - 功能: 实验运行日志的数据模型
 * - 表名: expt_run_log
 * - 字段定义:
 * * id: Long - id
 * * spaceId: Long - 空间id
 * * createdBy: String - 创建者id
 * * exptId: Long - 实验id
 * * exptRunId: Long - 运行id
 * * itemIds: byte[] - 组ids
 * * mode: Integer - 模式
 * * status: Long - 状态
 * * pendingCnt: Integer - item未执行数量
 * * successCnt: Integer - item成功数量
 * * failCnt: Integer - item失败数量
 * * creditCost: Double - credit消耗
 * * tokenCost: Long - token消耗
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * * statusMessage: byte[] - 提示信息
 * * processingCnt: Integer - processing_cnt
 * * terminatedCnt: Integer - terminated_cnt
 * <p>
 * Java实现说明:
 * - 对应Go的model.ExptRunLog结构体
 * - 使用JPA注解进行数据库映射
 * - 使用Lombok注解简化代码
 * - 支持软删除功能
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go gorm.DeletedAt -> Java Long
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go float64 -> Java Double
 * - Go string -> Java String
 * - Go *[]byte -> Java byte[]
 * - Go *int32 -> Java Integer
 * - Go *int64 -> Java Long
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expt_run_log")
public class ExptRunLogEntity {

  /**
   * id
   * 迁移对应关系: Go语言ExptRunLog.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间id
   * 迁移对应关系: Go语言ExptRunLog.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, uniqueIndex:uk_expt_run,priority:1, index:idx_expt_run_item_turn,priority:1
   */
  private Long spaceId;

  /**
   * 创建者id
   * 迁移对应关系: Go语言ExptRunLog.CreatedBy
   * - 功能: 创建者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 实验id
   * 迁移对应关系: Go语言ExptRunLog.ExptID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20), not null, uniqueIndex:uk_expt_run,priority:2, index:idx_expt_run_item_turn,priority:2
   */
  private Long exptId;

  /**
   * 运行id
   * 迁移对应关系: Go语言ExptRunLog.ExptRunID
   * - 功能: 运行标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20), not null, uniqueIndex:uk_expt_run,priority:3
   */
  private Long exptRunId;

  /**
   * 组ids
   * 迁移对应关系: Go语言ExptRunLog.ItemIds
   * - 功能: 组标识列表
   * - 类型: JSON字符串
   * - 数据库: text
   */
  private String itemIds;

  /**
   * 模式
   * 迁移对应关系: Go语言ExptRunLog.Mode
   * - 功能: 运行模式
   * - 类型: Go的*int32对应Java的Integer
   * - 数据库: int(11)
   */
  private Integer mode;

  /**
   * 状态
   * 迁移对应关系: Go语言ExptRunLog.Status
   * - 功能: 运行状态
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20)
   */
  private Long status;

  /**
   * item未执行数量
   * 迁移对应关系: Go语言ExptRunLog.PendingCnt
   * - 功能: 未执行的数据项数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer pendingCnt;

  /**
   * item成功数量
   * 迁移对应关系: Go语言ExptRunLog.SuccessCnt
   * - 功能: 成功执行的数据项数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer successCnt;

  /**
   * item失败数量
   * 迁移对应关系: Go语言ExptRunLog.FailCnt
   * - 功能: 失败执行的数据项数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer failCnt;

  /**
   * credit消耗
   * 迁移对应关系: Go语言ExptRunLog.CreditCost
   * - 功能: 消耗的credit数量
   * - 类型: Go的float64对应Java的Double
   * - 数据库: decimal(15,2), not null, default:0.00
   */
  private Double creditCost;

  /**
   * token消耗
   * 迁移对应关系: Go语言ExptRunLog.TokenCost
   * - 功能: 消耗的token数量
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20)
   */
  private Long tokenCost;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptRunLog.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言ExptRunLog.UpdatedAt
   * - 功能: 记录最后更新时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言ExptRunLog.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的gorm.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;

  /**
   * 提示信息
   * 迁移对应关系: Go语言ExptRunLog.StatusMessage
   * - 功能: 状态相关的提示信息
   * - 类型: JSON字符串
   * - 数据库: text
   */
  private String statusMessage;

  /**
   * processing_cnt
   * 迁移对应关系: Go语言ExptRunLog.ProcessingCnt
   * - 功能: 处理中的数据项数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer processingCnt;

  /**
   * terminated_cnt
   * 迁移对应关系: Go语言ExptRunLog.TerminatedCnt
   * - 功能: 终止的数据项数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer terminatedCnt;
}
