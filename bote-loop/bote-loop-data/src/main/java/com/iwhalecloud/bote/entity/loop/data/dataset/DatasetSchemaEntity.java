package com.iwhalecloud.bote.entity.loop.data.dataset;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集Schema表持久化对象
 * 迁移对应关系: Go语言model.DatasetSchema
 * - 功能: 数据集Schema表的数据模型
 * - 表名: bt_dataset_schema
 * - 字段定义:
 * * id: Long - 主键id
 * * appId: Integer - 应用ID
 * * spaceId: Long - 空间ID
 * * datasetId: Long - 数据集ID
 * * fields: String - 字段格式
 * * immutable: Boolean - 是否不允许编辑
 * * createdBy: String - 创建人
 * * createdAt: LocalDateTime - 创建时间
 * * updatedBy: String - 修改人
 * * updatedAt: LocalDateTime - 修改时间
 * * updateVersion: Long - 更新版本号
 * <p>
 * Java实现说明:
 * - 对应Go的model.DatasetSchema结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go datatypes.JSON -> Java String
 * - Go bool -> Java Boolean
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bt_dataset_schema")
public class DatasetSchemaEntity {

  /**
   * 主键id
   * 迁移对应关系: Go语言DatasetSchema.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * 应用ID
   * 迁移对应关系: Go语言DatasetSchema.AppID
   * - 功能: 应用标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer appId;

  /**
   * 空间ID
   * 迁移对应关系: Go语言DatasetSchema.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 数据集ID
   * 迁移对应关系: Go语言DatasetSchema.DatasetID
   * - 功能: 数据集标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long datasetId;

  /**
   * 字段格式
   * 迁移对应关系: Go语言DatasetSchema.Fields
   * - 功能: 字段格式定义
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json, not null
   */
  private String fields;

  /**
   * 是否不允许编辑
   * 迁移对应关系: Go语言DatasetSchema.Immutable
   * - 功能: 是否不允许编辑
   * - 类型: Go的bool对应Java的String（存储'T'或'F'）
   * - 数据库: char(1), not null
   */
  private String immutable;

  /**
   * 创建人
   * 迁移对应关系: Go语言DatasetSchema.CreatedBy
   * - 功能: 创建人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言DatasetSchema.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 修改人
   * 迁移对应关系: Go语言DatasetSchema.UpdatedBy
   * - 功能: 修改人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String updatedBy;

  /**
   * 修改时间
   * 迁移对应关系: Go语言DatasetSchema.UpdatedAt
   * - 功能: 记录修改时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 更新版本号
   * 迁移对应关系: Go语言DatasetSchema.UpdateVersion
   * - 功能: 更新版本号
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long updateVersion;
}
