package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 函数DTO
 * 对应Thrift: Function
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "函数DTO")
public class FunctionDTO {

  /**
   * 函数名
   * 对应Thrift字段: name
   */
  @Schema(description = "函数名")
  private String name;

  /**
   * 函数描述
   * 对应Thrift字段: description
   */
  @Schema(description = "函数描述")
  private String description;

  /**
   * 函数参数
   * 对应Thrift字段: parameters
   */
  @Schema(description = "函数参数")
  private String parameters;
}
