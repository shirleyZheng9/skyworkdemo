package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt结果DTO
 * 对应Thrift: PromptResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptResult {

  @JsonProperty("query")
  private PromptQuery query;

  @JsonProperty("prompt")
  private PromptDTO prompt;
}
