package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目数据实体
 * 迁移对应关系: Go语言entity.ItemData
 * - 功能: 存储项目数据信息
 * - 字段定义:
 * * ID: int64 - 数据ID
 * * Data: []*FieldData - 字段数据列表
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ItemData结构体
 * - 使用Java类定义，包含项目数据字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go切片 -> Java List
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemData {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("data")
  private List<FieldData> data;
}
