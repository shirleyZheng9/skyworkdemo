package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息结构体
 * 迁移对应关系: Go语言Message
 * - 功能: 消息数据结构
 * - 字段: role, content, ext
 * <p>
 * Java实现说明:
 * - 对应Go的Message结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go Role -> Java Role
 * - Go *Content -> Java Content
 * - Go map[string]string -> Java Map<String, String>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
  @Schema(description = "角色")
  private Role role;

  @Schema(description = "内容")
  private Content content;

  @Schema(description = "扩展信息")
  private Map<String, String> ext;
}
