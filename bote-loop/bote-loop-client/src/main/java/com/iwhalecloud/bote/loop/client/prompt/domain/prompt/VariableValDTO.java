package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变量值DTO
 * 迁移对应关系: Thrift struct VariableVal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableValDTO {

  @Schema(description = "变量键")
  private String key;

  @Schema(description = "变量值")
  private String value;

  @Schema(description = "占位符消息列表")
  private List<MessageDTO> placeholderMessages;
}
