package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容部分实体
 * 迁移对应关系: Go语言entity.ContentPart
 * - 功能: 存储消息内容的不同部分
 * - 字段定义:
 * * Type: ContentType - 内容类型
 * * Text: *string - 文本内容
 * * ImageURL: *ImageURL - 图片URL
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ContentPart结构体
 * - 使用Java类定义，包含内容部分字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go枚举类型 -> Java枚举
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentPart {

  /**
   * 内容类型
   * 迁移对应关系: Go语言entity.ContentPart.Type (ContentType)
   * - 功能: 内容部分的类型标识
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 区分不同类型的内容
   */
  @JsonProperty("type")
  private ContentType type;

  /**
   * 文本内容
   * 迁移对应关系: Go语言entity.ContentPart.Text (*string)
   * - 功能: 文本类型的内容
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储文本内容
   */
  @JsonProperty("text")
  private String text;

  /**
   * 图片URL
   * 迁移对应关系: Go语言entity.ContentPart.ImageURL (*ImageURL)
   * - 功能: 图片类型的内容
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储图片URL信息
   */
  @JsonProperty("image_url")
  private ImageURL imageUrl;
}
