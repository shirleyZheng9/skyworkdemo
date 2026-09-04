package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连续数字规格实体
 * 迁移对应关系: Go语言entity.ContinuousNumberSpec
 * - 功能: 存储连续数字类型标签的规格配置
 * - 字段定义:
 * * MinValue: *float64 - 最小值
 * * MinValueDesc: *string - 最小值描述
 * * MaxValue: *float64 - 最大值
 * * MaxValueDesc: *string - 最大值描述
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ContinuousNumberSpec结构体
 * - 使用Java类定义，包含数字规格字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContinuousNumberSpec {

  /**
   * 最小值
   * 迁移对应关系: Go语言entity.ContinuousNumberSpec.MinValue (*float64)
   * - 功能: 数字范围的最小值
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 数值验证
   */
  @JsonProperty("min_value")
  private Double minValue;

  /**
   * 最小值描述
   * 迁移对应关系: Go语言entity.ContinuousNumberSpec.MinValueDesc (*string)
   * - 功能: 最小值的描述信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 用户界面提示
   */
  @JsonProperty("min_value_desc")
  private String minValueDesc;

  /**
   * 最大值
   * 迁移对应关系: Go语言entity.ContinuousNumberSpec.MaxValue (*float64)
   * - 功能: 数字范围的最大值
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 数值验证
   */
  @JsonProperty("max_value")
  private Double maxValue;

  /**
   * 最大值描述
   * 迁移对应关系: Go语言entity.ContinuousNumberSpec.MaxValueDesc (*string)
   * - 功能: 最大值的描述信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 用户界面提示
   */
  @JsonProperty("max_value_desc")
  private String maxValueDesc;
}
