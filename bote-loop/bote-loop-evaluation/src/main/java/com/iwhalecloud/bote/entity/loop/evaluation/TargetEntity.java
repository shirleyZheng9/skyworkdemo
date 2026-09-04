package com.iwhalecloud.bote.entity.loop.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评估对象信息持久化对象
 * 迁移对应关系: Go语言model.Target
 * - 功能: 评估对象表的数据模型
 * - 表名: eval_target
 * - 字段定义:
 * * id: Long - idgen id
 * * spaceId: Long - 空间id
 * * sourceTargetId: String - 来源的对象的ID，比如promptID
 * * targetType: Integer - 评估对象类型
 * * createdBy: String - 创建人
 * * updatedBy: String - 更新人
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: LocalDateTime - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.Target结构体
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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetEntity {

  /**
   * idgen id
   * 迁移对应关系: Go语言Target.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, comment:idgen id
   */
  private Long id;

  /**
   * 空间id
   * 迁移对应关系: Go语言Target.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, uniqueIndex, comment:空间id
   */
  private Long spaceId;

  /**
   * 来源的对象的ID，比如promptID
   * 迁移对应关系: Go语言Target.SourceTargetID
   * - 功能: 来源对象标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null, uniqueIndex, comment:来源的对象的ID，比如promptID
   */
  private String sourceTargetId;

  /**
   * 评估对象类型
   * 迁移对应关系: Go语言Target.TargetType
   * - 功能: 评估对象类型标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null, uniqueIndex, comment:评估对象类型
   */
  private Integer targetType;

  /**
   * 创建人
   * 迁移对应关系: Go语言Target.CreatedBy
   * - 功能: 创建者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null, default:0, comment:创建人
   */
  private String createdBy;

  /**
   * 更新人
   * 迁移对应关系: Go语言Target.UpdatedBy
   * - 功能: 更新者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null, default:0, comment:更新人
   */
  private String updatedBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言Target.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP, comment:创建时间
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言Target.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP, comment:更新时间
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言Target.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的gorm.DeletedAt对应Java的Date
   * - 数据库: timestamp, comment:删除时间
   */
  private Date deletedAt;
}
