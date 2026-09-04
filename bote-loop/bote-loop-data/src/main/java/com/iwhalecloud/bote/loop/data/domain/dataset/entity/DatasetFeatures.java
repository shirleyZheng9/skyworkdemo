package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集功能实体
 * 迁移对应关系: Go语言entity.DatasetFeatures
 * - 功能: 存储数据集的功能开关信息
 * - 字段定义:
 * * EditSchema: bool - 变更 schema
 * * RepeatedData: bool - 多轮数据
 * * MultiModal: bool - 多模态
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetFeatures结构体
 * - 使用Java类定义，包含功能字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go bool -> Java Boolean
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetFeatures {

  @JsonProperty("edit_schema")
  private Boolean editSchema;

  @JsonProperty("repeated_data")
  private Boolean repeatedData;

  @JsonProperty("multi_modal")
  private Boolean multiModal;
}
