package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对象存储实体
 * 迁移对应关系: Go语言entity.ObjectStorage
 * - 功能: 存储对象存储信息
 * - 字段定义:
 * * Provider: entity.Provider - 存储提供商
 * * Name: string - 对象名称
 * * URI: string - 对象URI
 * * URL: string - 访问URL
 * * ThumbURL: string - 缩略图URL
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ObjectStorage结构体
 * - 使用Java类定义，包含对象存储字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go枚举类型 -> Java枚举
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectStorage {

  @JsonProperty("provider")
  private String provider;

  @JsonProperty("name")
  private String name;

  @JsonProperty("uri")
  private String uri;

  @JsonProperty("url")
  private String url;

  @JsonProperty("thumb_url")
  private String thumbUrl;
}
