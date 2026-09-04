package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 音频结构体
 * 迁移对应关系: Go语言Audio
 * - 功能: 音频数据结构
 * - 字段: format, url
 * <p>
 * Java实现说明:
 * - 对应Go的Audio结构体
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
public class Audio {
  @Schema(description = "音频格式")
  private String format;

  @Schema(description = "音频URL")
  private String url;
}
