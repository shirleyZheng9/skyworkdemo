package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugToolCall {

  @JsonProperty("tool_call")
  private ToolCall toolCall;

  @JsonProperty("mock_response")
  private String mockResponse;

  @JsonProperty("debug_trace_key")
  private String debugTraceKey;

}
