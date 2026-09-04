package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集版本实体
 * 迁移对应关系: Go语言entity.DatasetVersion
 * - 功能: 存储数据集版本信息
 * - 字段定义:
 * * ID: int64 - 版本ID
 * * AppID: int32 - 应用ID
 * * SpaceID: int64 - 空间ID
 * * DatasetID: int64 - 数据集ID
 * * SchemaID: int64 - Schema ID
 * * DatasetBrief: *Dataset - 数据集元信息备份
 * * Version: string - 版本号，SemVer2 三段式
 * * VersionNum: int64 - 数字版本号，从1开始递增
 * * Description: *string - 版本描述
 * * ItemCount: int64 - 条数
 * * SnapshotStatus: SnapshotStatus - 快照状态
 * * SnapshotProgress: *SnapshotProgress - 快照进度
 * * UpdateVersion: int64 - 更新版本号，用于乐观锁
 * * CreatedBy: string - 创建者
 * * CreatedAt: time.Time - 创建时间
 * * DisabledAt: *time.Time - 版本禁用时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetVersion结构体
 * - 使用Java类定义，包含版本字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 包含ID访问方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go time.Time -> Java LocalDateTime
 * - Go方法 -> Java方法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetVersion {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("schema_id")
  private Long schemaId;

  @JsonProperty("dataset_brief")
  private Dataset datasetBrief;

  @JsonProperty("version")
  private String version;

  @JsonProperty("version_num")
  private Long versionNum;

  @JsonProperty("description")
  private String description;

  @JsonProperty("item_count")
  private Long itemCount;

  @JsonProperty("snapshot_status")
  private SnapshotStatus snapshotStatus;

  @JsonProperty("snapshot_progress")
  private SnapshotProgress snapshotProgress;

  @JsonProperty("update_version")
  private Long updateVersion;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Date createdAt;

  @JsonProperty("disabled_at")
  private Date disabledAt;
}
