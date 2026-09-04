package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息DTO
 * 对应Thrift: Message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "消息DTO")
public class MessageDTO {

  /**
   * 角色
   * 对应Thrift字段: role
   */
  @Schema(description = "角色")
  private RoleDTO role;

  /**
   * 内容
   * 对应Thrift字段: content
   */
  @Schema(description = "内容")
  private String content;
}
