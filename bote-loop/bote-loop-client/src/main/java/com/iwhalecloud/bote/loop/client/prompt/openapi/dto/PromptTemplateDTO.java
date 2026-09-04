package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt模板DTO
 * 对应Thrift: PromptTemplate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Prompt模板DTO")
public class PromptTemplateDTO {

  /**
   * 模板类型
   * 对应Thrift字段: template_type
   */
  @Schema(description = "模板类型")
  private TemplateTypeDTO templateType;

  /**
   * 消息列表
   * 对应Thrift字段: messages
   */
  @Schema(description = "消息列表")
  private List<MessageDTO> messages;

  /**
   * 变量定义列表
   * 对应Thrift字段: variable_defs
   */
  @Schema(description = "变量定义列表")
  private List<VariableDefDTO> variableDefs;
}
