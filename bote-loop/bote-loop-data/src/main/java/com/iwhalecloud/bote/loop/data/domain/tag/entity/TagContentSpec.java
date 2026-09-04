package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签内容规格实体
 * 迁移对应关系: Go语言entity.TagContentSpec
 * - 功能: 存储标签内容规格配置
 * - 字段定义:
 * * ContinuousNumberSpec: *ContinuousNumberSpec - 连续数字规格
 * <p>
 * Java实现说明:
 * - 对应Go的entity.TagContentSpec结构体
 * - 使用Java类定义，包含内容规格字段
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
public class TagContentSpec {

  /**
   * 连续数字规格
   * 迁移对应关系: Go语言entity.TagContentSpec.ContinuousNumberSpec (*ContinuousNumberSpec)
   * - 功能: 连续数字类型标签的规格配置
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 定义数字范围等规则
   */
  @JsonProperty("continuous_number_spec")
  private ContinuousNumberSpec continuousNumberSpec;
}
