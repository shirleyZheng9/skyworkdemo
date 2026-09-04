package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片结构体
 * 迁移对应关系: Go语言Image
 * - 功能: 图片数据结构
 * - 字段: name, url, uri, thumbUrl
 * <p>
 * Java实现说明:
 * - 对应Go的Image结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {
  @Schema(description = "图片名称")
  private String name;

  @Schema(description = "图片URL")
  private String url;

  @Schema(description = "图片URI")
  private String uri;

  @Schema(description = "缩略图URL")
  private String thumbUrl;
}
