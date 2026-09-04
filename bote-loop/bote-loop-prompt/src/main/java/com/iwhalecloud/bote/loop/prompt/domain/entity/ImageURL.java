package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片URL实体
 * 迁移对应关系: Go语言entity.ImageURL
 * - 功能: 存储图片URL信息
 * - 字段定义:
 * * URI: string - URI地址
 * * URL: string - URL地址
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ImageURL结构体
 * - 使用Java类定义，包含图片URL字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageURL {

  /**
   * URI地址
   * 迁移对应关系: Go语言entity.ImageURL.URI (string)
   * - 功能: 图片的URI地址
   * - 类型: Go的string对应Java的String
   * - 用途: 图片资源标识
   */
  @JsonProperty("uri")
  private String uri;

  /**
   * URL地址
   * 迁移对应关系: Go语言entity.ImageURL.URL (string)
   * - 功能: 图片的URL地址
   * - 类型: Go的string对应Java的String
   * - 用途: 图片资源访问地址
   */
  @JsonProperty("url")
  private String url;
}
