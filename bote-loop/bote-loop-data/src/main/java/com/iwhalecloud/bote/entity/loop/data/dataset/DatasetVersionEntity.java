package com.iwhalecloud.bote.entity.loop.data.dataset;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集版本表持久化对象
 * 迁移对应关系: Go语言model.DatasetVersion
 * - 功能: 数据集版本表的数据模型
 * - 表名: bt_dataset_version
 * - 字段定义:
 * * id: Long - 主键id
 * * appId: Integer - 应用ID
 * * spaceId: Long - 空间ID
 * * datasetId: Long - 数据集ID
 * * schemaId: Long - Schema ID
 * * datasetBrief: String - 数据集元信息备份
 * * version: String - 版本号，SemVer2 三段式
 * * versionNum: Long - 数字版本号，从1开始递增
 * * description: String - 版本描述
 * * itemCount: Long - 条数
 * * snapshotStatus: String - 快照状态
 * * snapshotProgress: String - 快照进度详情
 * * updateVersion: Long - 更新版本号
 * * createdBy: String - 创建人
 * * createdAt: LocalDateTime - 创建时间
 * * disabledAt: LocalDateTime - 版本禁用时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.DatasetVersion结构体
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
 * - Go *string -> Java String
 * - Go *time.Time -> Java LocalDateTime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bt_dataset_version")
public class DatasetVersionEntity {

  /**
   * 主键id
   * 迁移对应关系: Go语言DatasetVersion.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * 应用ID
   * 迁移对应关系: Go语言DatasetVersion.AppID
   * - 功能: 应用标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer appId;

  /**
   * 空间ID
   * 迁移对应关系: Go语言DatasetVersion.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 数据集ID
   * 迁移对应关系: Go语言DatasetVersion.DatasetID
   * - 功能: 数据集标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long datasetId;

  /**
   * Schema ID
   * 迁移对应关系: Go语言DatasetVersion.SchemaID
   * - 功能: Schema标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long schemaId;

  /**
   * 数据集元信息备份
   * 迁移对应关系: Go语言DatasetVersion.DatasetBrief
   * - 功能: 数据集元信息备份
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String datasetBrief;

  /**
   * 版本号，SemVer2 三段式
   * 迁移对应关系: Go语言DatasetVersion.Version
   * - 功能: 版本号
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(64), not null
   */
  private String version;

  /**
   * 数字版本号，从1开始递增
   * 迁移对应关系: Go语言DatasetVersion.VersionNum
   * - 功能: 数字版本号
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, default:1
   */
  private Long versionNum;

  /**
   * 版本描述
   * 迁移对应关系: Go语言DatasetVersion.Description
   * - 功能: 版本描述
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(2048)
   */
  private String description;

  /**
   * 条数
   * 迁移对应关系: Go语言DatasetVersion.ItemCount
   * - 功能: 条数
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long itemCount;

  /**
   * 快照状态
   * 迁移对应关系: Go语言DatasetVersion.SnapshotStatus
   * - 功能: 快照状态
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(64), not null
   */
  private String snapshotStatus;

  /**
   * 快照进度详情
   * 迁移对应关系: Go语言DatasetVersion.SnapshotProgress
   * - 功能: 快照进度详情
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String snapshotProgress;

  /**
   * 更新版本号
   * 迁移对应关系: Go语言DatasetVersion.UpdateVersion
   * - 功能: 更新版本号
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long updateVersion;

  /**
   * 创建人
   * 迁移对应关系: Go语言DatasetVersion.CreatedBy
   * - 功能: 创建人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言DatasetVersion.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 版本禁用时间
   * 迁移对应关系: Go语言DatasetVersion.DisabledAt
   * - 功能: 版本禁用时间
   * - 类型: Go的*time.Time对应Java的Date
   * - 数据库: timestamp
   */
  private Date disabledAt;
}
