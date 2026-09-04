package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评估器执行结果持久化对象
 * 迁移对应关系: Go语言model.EvaluatorRecord
 * - 功能: 评估器执行结果的数据模型
 * - 表名: evaluator_record
 * - 字段定义:
 * * id: Long - idgen id
 * * spaceId: Long - 空间id
 * * evaluatorVersionId: Long - 评估器版本id
 * * experimentId: Long - 实验id
 * * experimentRunId: Long - 实验执行id
 * * itemId: Long - 评估集行id
 * * turnId: Long - 评估集行轮次id
 * * logId: String - log id
 * * traceId: String - trace id
 * * score: Double - 得分
 * * status: Integer - 执行状态
 * * inputData: byte[] - 输入数据
 * * outputData: byte[] - 执行结果
 * * createdBy: String - 创建人
 * * updatedBy: String - 更新人
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * * ext: byte[] - 补充信息
 * <p>
 * Java实现说明:
 * - 对应Go的model.EvaluatorRecord结构体
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
 * - Go *float64 -> Java Double
 * - Go *[]byte -> Java byte[]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "evaluator_record")
public class EvaluatorRecordEntity {

  /**
   * idgen id
   * 迁移对应关系: Go语言EvaluatorRecord.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间id
   * 迁移对应关系: Go语言EvaluatorRecord.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 评估器版本id
   * 迁移对应关系: Go语言EvaluatorRecord.EvaluatorVersionID
   * - 功能: 评估器版本标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long evaluatorVersionId;

  /**
   * 实验id
   * 迁移对应关系: Go语言EvaluatorRecord.ExperimentID
   * - 功能: 实验标识
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20) unsigned
   */
  private Long experimentId;

  /**
   * 实验执行id
   * 迁移对应关系: Go语言EvaluatorRecord.ExperimentRunID
   * - 功能: 实验执行标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long experimentRunId;

  /**
   * 评估集行id
   * 迁移对应关系: Go语言EvaluatorRecord.ItemID
   * - 功能: 评估集行标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long itemId;

  /**
   * 评估集行轮次id
   * 迁移对应关系: Go语言EvaluatorRecord.TurnID
   * - 功能: 评估集行轮次标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long turnId;

  /**
   * log id
   * 迁移对应关系: Go语言EvaluatorRecord.LogID
   * - 功能: 日志标识
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(255)
   */
  private String logId;

  /**
   * trace id
   * 迁移对应关系: Go语言EvaluatorRecord.TraceID
   * - 功能: 追踪标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null
   */
  private String traceId;

  /**
   * 得分
   * 迁移对应关系: Go语言EvaluatorRecord.Score
   * - 功能: 评估得分
   * - 类型: Go的*float64对应Java的Double
   * - 数据库: decimal(10,4)
   */
  private Double score;

  /**
   * 执行状态
   * 迁移对应关系: Go语言EvaluatorRecord.Status
   * - 功能: 执行状态标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer status;

  /**
   * 输入数据
   * 迁移对应关系: Go语言EvaluatorRecord.InputData
   * - 功能: 输入数据JSON
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: mediumblob binary
   */
  private String inputData;

  /**
   * 执行结果
   * 迁移对应关系: Go语言EvaluatorRecord.OutputData
   * - 功能: 执行结果JSON
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: mediumblob binary
   */
  private String outputData;

  /**
   * 创建人
   * 迁移对应关系: Go语言EvaluatorRecord.CreatedBy
   * - 功能: 创建者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 更新人
   * 迁移对应关系: Go语言EvaluatorRecord.UpdatedBy
   * - 功能: 更新者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String updatedBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言EvaluatorRecord.CreatedAt
   * - 功能: 创建时间戳
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言EvaluatorRecord.UpdatedAt
   * - 功能: 更新时间戳
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言EvaluatorRecord.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;

  /**
   * 补充信息
   * 迁移对应关系: Go语言EvaluatorRecord.Ext
   * - 功能: 补充信息JSON
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: mediumblob binary
   */
  private String ext;
}
