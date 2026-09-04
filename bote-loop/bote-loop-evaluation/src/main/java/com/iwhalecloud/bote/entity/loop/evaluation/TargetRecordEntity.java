package com.iwhalecloud.bote.entity.loop.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评估对象记录信息持久化对象
 * 迁移对应关系: Go语言model.TargetRecord
 * - 功能: 评估对象记录表的数据模型
 * - 表名: eval_target_record
 * - 字段定义:
 * * id: Long - id
 * * spaceId: Long - 空间id
 * * targetId: Long - 评测对象id
 * * targetVersionId: Long - 版本ID
 * * experimentRunId: Long - 实验执行id
 * * itemId: Long - 评测集行id
 * * turnId: Long - 评测集行轮次id
 * * logId: String - log id
 * * traceId: String - trace id
 * * inputData: byte[] - 输入, json
 * * outputData: byte[] - 输出, json
 * * status: Integer - 执行状态
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: LocalDateTime - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.TargetRecord结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 支持软删除功能
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go gorm.DeletedAt -> Java LocalDateTime
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go *[]byte -> Java byte[]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetRecordEntity {

  /**
   * id
   * 迁移对应关系: Go语言TargetRecord.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, comment:id
   */
  private Long id;

  /**
   * 空间id
   * 迁移对应关系: Go语言TargetRecord.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, comment:空间id
   */
  private Long spaceId;

  /**
   * 评测对象id
   * 迁移对应关系: Go语言TargetRecord.TargetID
   * - 功能: 评测对象标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, comment:评测对象id
   */
  private Long targetId;

  /**
   * 版本ID
   * 迁移对应关系: Go语言TargetRecord.TargetVersionID
   * - 功能: 版本标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, comment:版本ID
   */
  private Long targetVersionId;

  /**
   * 实验执行id
   * 迁移对应关系: Go语言TargetRecord.ExperimentRunID
   * - 功能: 实验执行标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, comment:实验执行id
   */
  private Long experimentRunId;

  /**
   * 评测集行id
   * 迁移对应关系: Go语言TargetRecord.ItemID
   * - 功能: 评测集行标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, comment:评测集行id
   */
  private Long itemId;

  /**
   * 评测集行轮次id
   * 迁移对应关系: Go语言TargetRecord.TurnID
   * - 功能: 评测集行轮次标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, comment:评测集行轮次id
   */
  private Long turnId;

  /**
   * log id
   * 迁移对应关系: Go语言TargetRecord.LogID
   * - 功能: 日志标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null, comment:log id
   */
  private String logId;

  /**
   * trace id
   * 迁移对应关系: Go语言TargetRecord.TraceID
   * - 功能: 链路追踪标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null, comment:trace id
   */
  private String traceId;

  /**
   * 输入, json
   * 迁移对应关系: Go语言TargetRecord.InputData
   * - 功能: 输入数据
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: mediumblob binary, comment:输入, json
   */
  private String inputData;

  /**
   * 输出, json
   * 迁移对应关系: Go语言TargetRecord.OutputData
   * - 功能: 输出数据
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: mediumblob binary, comment:输出, json
   */
  private String outputData;

  /**
   * 执行状态
   * 迁移对应关系: Go语言TargetRecord.Status
   * - 功能: 执行状态
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null, comment:执行状态
   */
  private Integer status;

  /**
   * 创建时间
   * 迁移对应关系: Go语言TargetRecord.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP, comment:创建时间
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言TargetRecord.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP, comment:更新时间
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言TargetRecord.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的gorm.DeletedAt对应Java的Date
   * - 数据库: timestamp, comment:删除时间
   */
  private Long deletedAt;
}
