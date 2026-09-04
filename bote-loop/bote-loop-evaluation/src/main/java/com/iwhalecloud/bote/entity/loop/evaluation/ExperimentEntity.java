package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 实验持久化对象
 * 迁移对应关系: Go语言model.Experiment
 * - 功能: 实验的数据模型
 * - 表名: experiment
 * - 字段定义:
 * * id: Long - id
 * * spaceId: Long - 空间 id
 * * createdBy: String - 创建者 id
 * * name: String - 实验名称
 * * description: String - 实验描述
 * * evalSetVersionId: Long - 评测集版本 id
 * * targetType: Long - 评估对象类型
 * * targetVersionId: Long - 评估对象版本 id
 * * evalConf: byte[] - 实验评估流程配置
 * * status: Integer - 状态
 * * statusMessage: byte[] - 状态提示信息
 * * startAt: LocalDateTime - 开始执行时间
 * * endAt: LocalDateTime - 结束执行时间
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * * latestRunId: Long - 最后运行id
 * * targetId: Long - 评估对象 id
 * * evalSetId: Long - 评测集 id
 * * creditCost: Integer - 权益消耗模式
 * * sourceType: Integer - 实验来源类型，评测:1,自动化任务:2...
 * * sourceId: String - 实验来源id
 * * exptType: Integer - 实验类型，offline:1,online:2...
 * * maxAliveTime: Long - 最大存活时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.Experiment结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 支持软删除功能
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java Date
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
@Table(name = "experiment")
public class ExperimentEntity {

  /**
   * id
   * 迁移对应关系: Go语言Experiment.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间 id
   * 迁移对应关系: Go语言Experiment.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 创建者 id
   * 迁移对应关系: Go语言Experiment.CreatedBy
   * - 功能: 创建者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128) character set utf8mb4, not null
   */
  private String createdBy;

  /**
   * 实验名称
   * 迁移对应关系: Go语言Experiment.Name
   * - 功能: 实验名称
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255) character set utf8mb4, not null
   */
  private String name;

  /**
   * 实验描述
   * 迁移对应关系: Go语言Experiment.Description
   * - 功能: 实验描述
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(1024) character set utf8mb4, not null
   */
  private String description;

  /**
   * 评测集版本 id
   * 迁移对应关系: Go语言Experiment.EvalSetVersionID
   * - 功能: 评测集版本标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long evalSetVersionId;

  /**
   * 评估对象类型
   * 迁移对应关系: Go语言Experiment.TargetType
   * - 功能: 评估对象类型
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long targetType;

  /**
   * 评估对象版本 id
   * 迁移对应关系: Go语言Experiment.TargetVersionID
   * - 功能: 评估对象版本标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long targetVersionId;

  /**
   * 实验评估流程配置
   * 迁移对应关系: Go语言Experiment.EvalConf
   * - 功能: 实验评估流程配置
   * - 类型: JSON字符串
   * - 数据库: text
   */
  private String evalConf;

  /**
   * 状态
   * 迁移对应关系: Go语言Experiment.Status
   * - 功能: 状态
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer status;

  /**
   * 状态提示信息
   * 迁移对应关系: Go语言Experiment.StatusMessage
   * - 功能: 状态提示信息
   * - 类型: JSON字符串
   * - 数据库: text
   */
  private String statusMessage;

  /**
   * 开始执行时间
   * 迁移对应关系: Go语言Experiment.StartAt
   * - 功能: 开始执行时间
   * - 类型: Go的*time.Time对应Java的Date
   * - 数据库: timestamp
   */
  private Date startAt;

  /**
   * 结束执行时间
   * 迁移对应关系: Go语言Experiment.EndAt
   * - 功能: 结束执行时间
   * - 类型: Go的*time.Time对应Java的Date
   * - 数据库: timestamp
   */
  private Date endAt;

  /**
   * 创建时间
   * 迁移对应关系: Go语言Experiment.CreatedAt
   * - 功能: 创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言Experiment.UpdatedAt
   * - 功能: 更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言Experiment.DeletedAt
   * - 功能: 删除时间
   * - 类型: Go的gorm.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;

  /**
   * 最后运行id
   * 迁移对应关系: Go语言Experiment.LatestRunID
   * - 功能: 最后运行标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long latestRunId;

  /**
   * 评估对象 id
   * 迁移对应关系: Go语言Experiment.TargetID
   * - 功能: 评估对象标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long targetId;

  /**
   * 评测集 id
   * 迁移对应关系: Go语言Experiment.EvalSetID
   * - 功能: 评测集标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long evalSetId;

  /**
   * 权益消耗模式
   * 迁移对应关系: Go语言Experiment.CreditCost
   * - 功能: 权益消耗模式
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer creditCost;

  /**
   * 实验来源类型，评测:1,自动化任务:2...
   * 迁移对应关系: Go语言Experiment.SourceType
   * - 功能: 实验来源类型
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null, default:1
   */
  private Integer sourceType;

  /**
   * 实验来源id
   * 迁移对应关系: Go语言Experiment.SourceID
   * - 功能: 实验来源标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128) character set utf8mb4, not null, default:0
   */
  private String sourceId;

  /**
   * 实验类型，offline:1,online:2...
   * 迁移对应关系: Go语言Experiment.ExptType
   * - 功能: 实验类型
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null, default:1
   */
  private Integer exptType;

  /**
   * 最大存活时间
   * 迁移对应关系: Go语言Experiment.MaxAliveTime
   * - 功能: 最大存活时间
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20) unsigned
   */
  private Long maxAliveTime;

  /**
   * 目录ID
   * - 功能: 目录标识
   * - 类型: Java的Long
   * - 数据库: bigint(20) unsigned
   */
  private Long catalogItemId;
}
