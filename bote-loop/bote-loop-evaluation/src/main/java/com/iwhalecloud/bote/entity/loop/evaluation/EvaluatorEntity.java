package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评估器信息持久化对象
 * 迁移对应关系: Go语言model.Evaluator
 * - 功能: 评估器信息的数据模型
 * - 表名: evaluator
 * - 字段定义:
 * * id: Long - idgen id
 * * spaceId: Long - 空间id
 * * evaluatorType: Integer - 评估器类型
 * * name: String - 名称
 * * description: String - 描述
 * * draftSubmitted: Boolean - 草稿是否已提交
 * * createdBy: String - 创建人
 * * updatedBy: String - 更新人
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * * latestVersion: String - 最新版本号
 * <p>
 * Java实现说明:
 * - 对应Go的model.Evaluator结构体
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
 * - Go *bool -> Java Boolean
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "evaluator")
public class EvaluatorEntity {

  /**
   * idgen id
   * 迁移对应关系: Go语言Evaluator.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间id
   * 迁移对应关系: Go语言Evaluator.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 评估器类型
   * 迁移对应关系: Go语言Evaluator.EvaluatorType
   * - 功能: 评估器类型标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer evaluatorType;

  /**
   * 名称
   * 迁移对应关系: Go语言Evaluator.Name
   * - 功能: 评估器名称
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(255)
   */
  private String name;

  /**
   * 描述
   * 迁移对应关系: Go语言Evaluator.Description
   * - 功能: 评估器描述
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(500)
   */
  private String description;

  /**
   * 草稿是否已提交
   * 迁移对应关系: Go语言Evaluator.DraftSubmitted
   * - 功能: 草稿提交状态
   * - 类型: Go的*bool对应Java的Boolean
   * - 数据库: tinyint(1)
   */
  private Boolean draftSubmitted;

  /**
   * 创建人
   * 迁移对应关系: Go语言Evaluator.CreatedBy
   * - 功能: 创建者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 更新人
   * 迁移对应关系: Go语言Evaluator.UpdatedBy
   * - 功能: 更新者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String updatedBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言Evaluator.CreatedAt
   * - 功能: 创建时间戳
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言Evaluator.UpdatedAt
   * - 功能: 更新时间戳
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言Evaluator.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Date deletedAt;

  /**
   * 最新版本号
   * 迁移对应关系: Go语言Evaluator.LatestVersion
   * - 功能: 最新版本标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String latestVersion;

  /**
   * 目录ID
   * - 功能: 目录标识
   * - 类型: Java的Long
   * - 数据库: bigint(20) unsigned
   */
  private Long catalogItemId;
}
