package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 项目实体
 * 迁移对应关系: Go语言entity.Item
 * - 功能: 存储数据集项目信息
 * - 字段定义:
 * * ID: int64 - 项目ID
 * * AppID: int32 - 应用ID
 * * SpaceID: int64 - 空间ID
 * * DatasetID: int64 - 数据集ID
 * * SchemaID: int64 - Schema ID
 * * ItemID: int64 - 项目ID
 * * ItemKey: string - 幂等 key
 * * Data: []*FieldData - 数据内容
 * * RepeatedData: []*ItemData - 多轮数据内容，与 Data 互斥
 * * DataProperties: *ItemDataProperties - 内容属性
 * * AddVN: int64 - 添加版本号
 * * DelVN: int64 - 删除版本号
 * * CreatedBy: string - 创建者
 * * CreatedAt: time.Time - 创建时间
 * * UpdatedBy: string - 更新者
 * * UpdatedAt: time.Time - 更新时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.Item结构体
 * - 使用Java类定义，包含项目字段
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
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

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

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("item_key")
  private String itemKey;

  @JsonProperty("data")
  private List<FieldData> data;

  @JsonProperty("repeated_data")
  private List<ItemData> repeatedData;

  @JsonProperty("data_properties")
  private ItemDataProperties dataProperties;

  @JsonProperty("add_vn")
  private Long addVN;

  @JsonProperty("del_vn")
  private Long delVN;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Date createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private Date updatedAt;

  /**
   * 获取或构建属性
   * 迁移对应关系: Go语言entity.Item.GetOrBuildProperties()
   * - 功能: 获取或构建项目数据属性
   * - 返回: 项目数据属性
   */
  public ItemDataProperties getOrBuildProperties() {
    if (dataProperties == null) {
      buildProperties();
    }
    return dataProperties;
  }

  /**
   * 构建属性
   * 迁移对应关系: Go语言entity.Item.BuildProperties()
   * - 功能: 构建项目数据属性
   */
  public void buildProperties() {
    List<List<FieldData>> allData = getAllData();
    dataProperties = ItemDataProperties.builder().build();

    long totalBytes = allData.stream()
      .flatMap(List::stream)
      .mapToLong(FieldData::getDataBytes)
      .sum();
    dataProperties.setBytes(totalBytes);

    long totalRunes = allData.stream()
      .flatMap(List::stream)
      .mapToLong(FieldData::getDataRunes)
      .sum();
    dataProperties.setRunes(totalRunes);
  }

  /**
   * 获取所有数据
   * 迁移对应关系: Go语言entity.Item.AllData()
   * - 功能: 获取所有数据内容
   * - 返回: 所有数据内容列表
   */
  public List<List<FieldData>> getAllData() {
    if (repeatedData != null && !repeatedData.isEmpty()) {
      return repeatedData.stream()
        .map(ItemData::getData)
        .collect(Collectors.toList());
    }
    return data != null ? List.of(data) : List.of();
  }

  /**
   * 清空数据
   * 迁移对应关系: Go语言entity.Item.ClearData()
   * - 功能: 清空项目数据
   */
  public void clearData() {
    this.data = null;
    this.repeatedData = null;
  }
}
