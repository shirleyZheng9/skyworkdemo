package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt模板DTO
 * 迁移对应关系: Thrift struct PromptTemplate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Prompt模板DTO")
public class PromptTemplateDTO {

  @Schema(description = "模板类型")
  private TemplateTypeDTO templateType;

  @Schema(description = "消息列表")
  private List<MessageDTO> messages;

  @Schema(description = "变量定义列表")
  private List<VariableDefDTO> variableDefs;
}
