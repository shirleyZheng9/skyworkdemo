package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变量定义DTO
 * 对应Thrift: VariableDef
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "变量定义DTO")
public class VariableDefDTO {

  /**
   * 变量键
   * 对应Thrift字段: key
   */
  @Schema(description = "变量键")
  private String key;

  /**
   * 变量描述
   * 对应Thrift字段: desc
   */
  @Schema(description = "变量描述")
  private String desc;

  /**
   * 变量类型
   * 对应Thrift字段: type
   */
  @Schema(description = "变量类型")
  private VariableTypeDTO type;
}
