package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 函数调用DTO
 * 迁移对应关系: Thrift struct FunctionCall
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "函数调用DTO")
public class FunctionCallDTO {

  @Schema(description = "函数名称")
  private String name;

  @Schema(description = "函数参数")
  private String arguments;
}
