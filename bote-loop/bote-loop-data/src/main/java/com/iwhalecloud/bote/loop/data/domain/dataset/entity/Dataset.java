package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据集实体
 * 迁移对应关系: Go语言entity.Dataset
 * - 功能: 存储数据集的基本信息
 * - 字段定义:
 * * ID: int64 - 数据集ID
 * * AppID: int32 - 应用ID
 * * SpaceID: int64 - 空间ID
 * * SchemaID: int64 - Schema ID
 * * Name: string - 数据集名称
 * * Description: *string - 数据集描述
 * * Category: DatasetCategory - 业务场景分类
 * * BizCategory: string - 业务场景下自定义分类
 * * Status: DatasetStatus - 状态
 * * SecurityLevel: SecurityLevel - 安全等级
 * * Visibility: DatasetVisibility - 可见性
 * * Spec: *DatasetSpec - 规格
 * * Features: *DatasetFeatures - 功能开关
 * * LatestVersion: string - 最新的版本号
 * * NextVersionNum: int64 - 下一个版本的数字版本号
 * * LastOperation: DatasetOpType - 最近一次操作
 * * CreatedBy: string - 创建者
 * * CreatedAt: time.Time - 创建时间
 * * UpdatedBy: string - 更新者
 * * UpdatedAt: time.Time - 更新时间
 * * ExpiredAt: *time.Time - 过期时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.Dataset结构体
 * - 使用Java类定义，包含数据集字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 包含业务逻辑方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go枚举类型 -> Java枚举
 * - Go time.Time -> Java LocalDateTime
 * - Go方法 -> Java方法
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("schema_id")
  private Long schemaId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("category")
  private DatasetCategory category;

  @JsonProperty("biz_category")
  private String bizCategory;

  @JsonProperty("status")
  private DatasetStatus status;

  @JsonProperty("security_level")
  private SecurityLevel securityLevel;

  @JsonProperty("visibility")
  private DatasetVisibility visibility;

  @JsonProperty("spec")
  private DatasetSpec spec;

  @JsonProperty("features")
  private DatasetFeatures features;

  @JsonProperty("latest_version")
  private String latestVersion;

  @JsonProperty("next_version_num")
  private Long nextVersionNum;

  @JsonProperty("last_operation")
  private DatasetOpType lastOperation;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Date createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private Date updatedAt;

  @JsonProperty("expired_at")
  private Date expiredAt;

  @JsonProperty("catalog_item_id")
  private Long catalogItemId;
  /**
   * 判断是否有未提交的变更
   * 迁移对应关系: Go语言entity.Dataset.IsChangeUncommitted()
   * - 功能: 判断数据集是否有未提交的变更
   * - 返回: 是否有未提交的变更
   */
  public boolean isChangeUncommitted() {
    if (lastOperation == null) {
      return false;
    }
    return lastOperation != DatasetOpType.CREATE_DATASET &&
      lastOperation != DatasetOpType.CREATE_VERSION;
  }

  /**
   * 获取描述信息
   * 迁移对应关系: Go语言entity.Dataset.GetDescription()
   * - 功能: 获取数据集描述信息
   * - 返回: 描述信息，如果为null则返回空字符串
   */
  public String getDescription() {
    return description != null ? description : "";
  }

  /**
   * 判断是否可以写入项目
   * 迁移对应关系: Go语言entity.Dataset.CanWriteItem()
   * - 功能: 判断数据集是否可以写入项目
   * - 返回: 是否可以写入项目
   */
  public boolean canWriteItem() {
    return status != DatasetStatus.DELETED && status != DatasetStatus.EXPIRED;
  }
}
