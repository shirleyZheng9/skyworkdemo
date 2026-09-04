package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变量定义DTO
 * 迁移对应关系: Thrift struct VariableDef
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "变量定义DTO")
public class VariableDefDTO {

  @Schema(description = "变量键")
  private String key;

  @Schema(description = "变量描述")
  private String desc;

  @Schema(description = "变量类型")
  private VariableTypeDTO type;
}
