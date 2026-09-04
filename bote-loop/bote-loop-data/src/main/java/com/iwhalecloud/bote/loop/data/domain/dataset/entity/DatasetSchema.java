package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集Schema实体
 * 迁移对应关系: Go语言entity.DatasetSchema
 * - 功能: 存储数据集Schema信息
 * - 字段定义:
 * * ID: int64 - Schema ID
 * * AppID: int32 - 应用ID
 * * SpaceID: int64 - 空间ID
 * * DatasetID: int64 - 数据集ID
 * * Fields: []*FieldSchema - 字段格式
 * * Immutable: bool - 是否不允许编辑
 * * CreatedBy: string - 创建者
 * * CreatedAt: time.Time - 创建时间
 * * UpdatedBy: string - 更新者
 * * UpdatedAt: time.Time - 更新时间
 * * UpdateVersion: int64 - 更新版本号
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetSchema结构体
 * - 使用Java类定义，包含Schema字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 包含业务逻辑方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go切片 -> Java List
 * - Go time.Time -> Java LocalDateTime
 * - Go方法 -> Java方法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetSchema {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("fields")
  private List<FieldSchema> fields;

  @JsonProperty("immutable")
  private Boolean immutable;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Date createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private Date updatedAt;

  @JsonProperty("update_version")
  private Long updateVersion;

  /**
   * 获取可用字段
   * 迁移对应关系: Go语言entity.DatasetSchema.AvailableFields()
   * - 功能: 获取可用的字段列表
   * - 返回: 可用字段列表
   */
  public List<FieldSchema> getAvailableFields() {
    if (fields == null) {
      return List.of();
    }
    return fields.stream()
      .filter(field -> field.getStatus() == FieldStatus.AVAILABLE || field.getStatus() == null)
      .collect(Collectors.toList());
  }

  /**
   * 获取ID
   * 迁移对应关系: Go语言entity.DatasetSchema.GetID()
   * - 功能: 获取Schema ID
   * - 返回: Schema ID
   */
  public Long getId() {
    return id;
  }

  /**
   * 设置ID
   * 迁移对应关系: Go语言entity.DatasetSchema.SetID()
   * - 功能: 设置Schema ID
   * - 参数: id - Schema ID
   */
  public void setId(Long id) {
    this.id = id;
  }
}
