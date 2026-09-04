package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟工具实体
 * 迁移对应关系: Go语言entity.MockTool
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockTool {

  @JsonProperty("name")
  private String name;

  @JsonProperty("mock_response")
  private String mockResponse;

}
