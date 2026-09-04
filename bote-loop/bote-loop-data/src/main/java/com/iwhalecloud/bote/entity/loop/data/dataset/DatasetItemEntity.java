package com.iwhalecloud.bote.entity.loop.data.dataset;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集条目表持久化对象
 * 迁移对应关系: Go语言model.DatasetItem
 * - 功能: 数据集条目表的数据模型
 * - 表名: bt_dataset_item
 * - 字段定义:
 * * id: Long - 主键id
 * * appId: Integer - 应用ID
 * * spaceId: Long - 空间ID
 * * datasetId: Long - 数据集ID
 * * schemaId: Long - Schema ID
 * * itemId: Long - 条目ID
 * * itemKey: String - 幂等key
 * * data: String - 数据内容
 * * repeatedData: String - 多轮数据内容
 * * dataProperties: String - 内容属性
 * * addVn: Long - 添加版本号
 * * delVn: Long - 删除版本号
 * * createdBy: String - 创建人
 * * createdAt: Date - 创建时间
 * * updatedBy: String - 修改人
 * * updatedAt: Date - 修改时间
 * * deletedAt: Long - 删除时间
 * * updateVersion: Long - 更新版本号，用于乐观锁
 * <p>
 * Java实现说明:
 * - 对应Go的model.DatasetItem结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java Date
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
@Table(name = "bt_dataset_item")
public class DatasetItemEntity {

  /**
   * 主键id
   * 迁移对应关系: Go语言DatasetItem.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * 应用ID
   * 迁移对应关系: Go语言DatasetItem.AppID
   * - 功能: 应用标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: bigint(20) unsigned, not null
   */
  private Integer appId;

  /**
   * 空间ID
   * 迁移对应关系: Go语言DatasetItem.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 数据集ID
   * 迁移对应关系: Go语言DatasetItem.DatasetID
   * - 功能: 数据集标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long datasetId;

  /**
   * Schema ID
   * 迁移对应关系: Go语言DatasetItem.SchemaID
   * - 功能: Schema标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long schemaId;

  /**
   * 条目ID
   * 迁移对应关系: Go语言DatasetItem.ItemID
   * - 功能: 条目标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long itemId;

  /**
   * 幂等key
   * 迁移对应关系: Go语言DatasetItem.ItemKey
   * - 功能: 幂等key
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String itemKey;

  /**
   * 数据内容
   * 迁移对应关系: Go语言DatasetItem.Data
   * - 功能: 数据内容
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String data;

  /**
   * 多轮数据内容
   * 迁移对应关系: Go语言DatasetItem.RepeatedData
   * - 功能: 多轮数据内容
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String repeatedData;

  /**
   * 内容属性
   * 迁移对应关系: Go语言DatasetItem.DataProperties
   * - 功能: 内容属性
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String dataProperties;

  /**
   * 添加版本号
   * 迁移对应关系: Go语言DatasetItem.AddVn
   * - 功能: 添加版本号
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long addVn;

  /**
   * 删除版本号
   * 迁移对应关系: Go语言DatasetItem.DelVn
   * - 功能: 删除版本号
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long delVn;

  /**
   * 创建人
   * 迁移对应关系: Go语言DatasetItem.CreatedBy
   * - 功能: 创建人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言DatasetItem.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 修改人
   * 迁移对应关系: Go语言DatasetItem.UpdatedBy
   * - 功能: 修改人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String updatedBy;

  /**
   * 修改时间
   * 迁移对应关系: Go语言DatasetItem.UpdatedAt
   * - 功能: 记录修改时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言DatasetItem.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: bigint(20), not null
   */
  private Long deletedAt;

  /**
   * 更新版本号，用于乐观锁
   * 迁移对应关系: Go语言DatasetItem.UpdateVersion
   * - 功能: 更新版本号，用于乐观锁
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long updateVersion;
}
