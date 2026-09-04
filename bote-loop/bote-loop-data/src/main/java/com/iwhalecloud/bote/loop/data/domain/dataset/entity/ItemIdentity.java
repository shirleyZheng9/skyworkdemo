package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目身份实体
 * 迁移对应关系: Go语言entity.ItemIdentity
 * - 功能: 存储项目身份信息
 * - 字段定义:
 * * SpaceID: int64 - 空间ID
 * * DatasetID: int64 - 数据集ID
 * * ID: int64 - 项目ID
 * * ItemID: int64 - 项目ID
 * * AddVN: int64 - 添加版本号
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ItemIdentity结构体
 * - 使用Java类定义，包含项目身份字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemIdentity {

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("id")
  private Long id;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("add_vn")
  private Long addVN;
}
