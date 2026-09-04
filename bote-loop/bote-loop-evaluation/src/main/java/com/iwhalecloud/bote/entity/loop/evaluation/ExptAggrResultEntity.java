package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验聚合结果持久化对象
 * 迁移对应关系: Go语言model.ExptAggrResult
 * - 功能: 实验聚合结果的数据模型
 * - 表名: expt_aggr_result
 * - 字段定义:
 * * id: Long - idgen id
 * * spaceId: Long - 空间id
 * * experimentId: Long - 实验id
 * * fieldType: Integer - 聚合字段类型 1：评估器得分
 * * fieldKey: String - 聚合字段唯一标识
 * * score: Double - 聚合后的平均得分
 * * aggrResult: byte[] - 详细聚合结果
 * * version: Long - 版本号(用于乐观锁)
 * * status: Integer - 计算状态 1:idle 2: caculating
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.ExptAggrResult结构体
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
 * - Go *int32 -> Java Integer
 * - Go *float64 -> Java Double
 * - Go *[]byte -> Java byte[]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expt_aggr_result")
public class ExptAggrResultEntity {

  /**
   * idgen id
   * 迁移对应关系: Go语言ExptAggrResult.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间id
   * 迁移对应关系: Go语言ExptAggrResult.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 实验id
   * 迁移对应关系: Go语言ExptAggrResult.ExperimentID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long experimentId;

  /**
   * 聚合字段类型 1：评估器得分
   * 迁移对应关系: Go语言ExptAggrResult.FieldType
   * - 功能: 聚合字段类型
   * - 类型: Go的*int32对应Java的Integer
   * - 数据库: int(11)
   */
  private Integer fieldType;

  /**
   * 聚合字段唯一标识
   * 迁移对应关系: Go语言ExptAggrResult.FieldKey
   * - 功能: 聚合字段唯一标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null
   */
  private String fieldKey;

  /**
   * 聚合后的平均得分
   * 迁移对应关系: Go语言ExptAggrResult.Score
   * - 功能: 聚合后的平均得分
   * - 类型: Go的*float64对应Java的Double
   * - 数据库: decimal(10,4)
   */
  private Double score;

  /**
   * 详细聚合结果
   * 迁移对应关系: Go语言ExptAggrResult.AggrResult
   * - 功能: 详细聚合结果
   * - 类型: JSON字符串
   * - 数据库: text
   */
  private String aggrResult;

  /**
   * 版本号(用于乐观锁)
   * 迁移对应关系: Go语言ExptAggrResult.Version
   * - 功能: 版本号(用于乐观锁)
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long version;

  /**
   * 计算状态 1:idle 2: caculating
   * 迁移对应关系: Go语言ExptAggrResult.Status
   * - 功能: 计算状态
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer status;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptAggrResult.CreatedAt
   * - 功能: 创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言ExptAggrResult.UpdatedAt
   * - 功能: 更新时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言ExptAggrResult.DeletedAt
   * - 功能: 删除时间
   * - 类型: Go的gorm.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;
}
