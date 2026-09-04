package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 实验统计持久化对象
 * 迁移对应关系: Go语言model.ExptStats
 * - 功能: 实验统计的数据模型
 * - 表名: expt_stats
 * - 字段定义:
 * * id: Long - id
 * * spaceId: Long - 空间 id
 * * exptId: Long - 实验 id
 * * pendingCnt: Integer - pending_cnt
 * * successCnt: Integer - success_cnt
 * * failCnt: Integer - fail_cnt
 * * creditCost: Double - credit 消耗
 * * inputTokenCost: Long - input token 消耗
 * * outputTokenCost: Long - output token 消耗
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * * processingCnt: Integer - processing_cnt
 * * terminatedCnt: Integer - terminated_cnt
 * <p>
 * Java实现说明:
 * - 对应Go的model.ExptStats结构体
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
 * - Go int32 -> Java Integer
 * - Go float64 -> Java Double
 * - Go *int64 -> Java Long
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expt_stats")
public class ExptStatsEntity {

  /**
   * id
   * 迁移对应关系: Go语言ExptStats.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间 id
   * 迁移对应关系: Go语言ExptStats.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 实验 id
   * 迁移对应关系: Go语言ExptStats.ExptID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long exptId;

  /**
   * pending_cnt
   * 迁移对应关系: Go语言ExptStats.PendingCnt
   * - 功能: 待处理数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer pendingCnt;

  /**
   * success_cnt
   * 迁移对应关系: Go语言ExptStats.SuccessCnt
   * - 功能: 成功数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer successCnt;

  /**
   * fail_cnt
   * 迁移对应关系: Go语言ExptStats.FailCnt
   * - 功能: 失败数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer failCnt;

  /**
   * credit 消耗
   * 迁移对应关系: Go语言ExptStats.CreditCost
   * - 功能: credit消耗
   * - 类型: Go的float64对应Java的Double
   * - 数据库: decimal(15,2), not null, default:0.00
   */
  private Double creditCost;

  /**
   * input token 消耗
   * 迁移对应关系: Go语言ExptStats.InputTokenCost
   * - 功能: input token消耗
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20)
   */
  private Long inputTokenCost;

  /**
   * output token 消耗
   * 迁移对应关系: Go语言ExptStats.OutputTokenCost
   * - 功能: output token消耗
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20)
   */
  private Long outputTokenCost;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptStats.CreatedAt
   * - 功能: 创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言ExptStats.UpdatedAt
   * - 功能: 更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言ExptStats.DeletedAt
   * - 功能: 删除时间
   * - 类型: Go的gorm.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;

  /**
   * processing_cnt
   * 迁移对应关系: Go语言ExptStats.ProcessingCnt
   * - 功能: 处理中数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer processingCnt;

  /**
   * terminated_cnt
   * 迁移对应关系: Go语言ExptStats.TerminatedCnt
   * - 功能: 终止数量
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer terminatedCnt;
}
