package com.iwhalecloud.bote.entity.loop.data.dataset;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集表持久化对象
 * 迁移对应关系: Go语言model.Dataset
 * - 功能: 数据集表的数据模型
 * - 表名: bt_dataset
 * - 字段定义:
 * * id: Long - 主键id
 * * appId: Integer - 应用ID
 * * spaceId: Long - 空间ID
 * * schemaId: Long - Schema ID
 * * name: String - 数据集名称
 * * description: String - 数据集描述
 * * category: String - 业务场景分类
 * * bizCategory: String - 业务场景下自定义分类
 * * status: String - 状态
 * * securityLevel: String - 安全等级
 * * visibility: String - 可见性
 * * spec: String - 规格配置
 * * features: String - 功能开关
 * * latestVersion: String - 最新版本号
 * * nextVersionNum: Long - 下一个版本的数字版本号
 * * lastOperation: String - 最新操作
 * * createdBy: String - 创建人
 * * createdAt: LocalDateTime - 创建时间
 * * updatedBy: String - 修改人
 * * updatedAt: LocalDateTime - 修改时间
 * * deletedAt: Long - 删除时间
 * * expiredAt: LocalDateTime - 过期时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.Dataset结构体
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
 * - Go soft_delete.DeletedAt -> Java Long
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bt_dataset")
public class DatasetEntity {

  /**
   * 主键id
   * 迁移对应关系: Go语言Dataset.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * 应用ID
   * 迁移对应关系: Go语言Dataset.AppID
   * - 功能: 应用标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer appId;

  /**
   * 空间ID
   * 迁移对应关系: Go语言Dataset.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * Schema ID
   * 迁移对应关系: Go语言Dataset.SchemaID
   * - 功能: Schema标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long schemaId;

  /**
   * 数据集名称
   * 迁移对应关系: Go语言Dataset.Name
   * - 功能: 数据集名称
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null
   */
  private String name;

  /**
   * 数据集描述
   * 迁移对应关系: Go语言Dataset.Description
   * - 功能: 数据集描述
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(2048)
   */
  private String description;

  /**
   * 业务场景分类
   * 迁移对应关系: Go语言Dataset.Category
   * - 功能: 业务场景分类
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(64), not null
   */
  private String category;

  /**
   * 业务场景下自定义分类
   * 迁移对应关系: Go语言Dataset.BizCategory
   * - 功能: 业务场景下自定义分类
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String bizCategory;

  /**
   * 状态
   * 迁移对应关系: Go语言Dataset.Status
   * - 功能: 数据集状态
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String status;

  /**
   * 安全等级
   * 迁移对应关系: Go语言Dataset.SecurityLevel
   * - 功能: 安全等级
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(32), not null
   */
  private String securityLevel;

  /**
   * 可见性
   * 迁移对应关系: Go语言Dataset.Visibility
   * - 功能: 可见性
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(64), not null
   */
  private String visibility;

  /**
   * 规格配置
   * 迁移对应关系: Go语言Dataset.Spec
   * - 功能: 规格配置
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String spec;

  /**
   * 功能开关
   * 迁移对应关系: Go语言Dataset.Features
   * - 功能: 功能开关
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String features;

  /**
   * 最新版本号
   * 迁移对应关系: Go语言Dataset.LatestVersion
   * - 功能: 最新版本号
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(64), not null
   */
  private String latestVersion;

  /**
   * 下一个版本的数字版本号
   * 迁移对应关系: Go语言Dataset.NextVersionNum
   * - 功能: 下一个版本的数字版本号
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, default:1
   */
  private Long nextVersionNum;

  /**
   * 最新操作
   * 迁移对应关系: Go语言Dataset.LastOperation
   * - 功能: 最新操作
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null
   */
  private String lastOperation;

  /**
   * 创建人
   * 迁移对应关系: Go语言Dataset.CreatedBy
   * - 功能: 创建人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言Dataset.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 修改人
   * 迁移对应关系: Go语言Dataset.UpdatedBy
   * - 功能: 修改人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String updatedBy;

  /**
   * 修改时间
   * 迁移对应关系: Go语言Dataset.UpdatedAt
   * - 功能: 记录修改时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言Dataset.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: bigint, not null
   */
  private Long deletedAt;

  /**
   * 过期时间
   * 迁移对应关系: Go语言Dataset.ExpiredAt
   * - 功能: 过期时间
   * - 类型: Go的*time.Time对应Java的Date
   * - 数据库: timestamp
   */
  private Date expiredAt;
  /** 目录ID */
  private Long catalogItemId;
}
