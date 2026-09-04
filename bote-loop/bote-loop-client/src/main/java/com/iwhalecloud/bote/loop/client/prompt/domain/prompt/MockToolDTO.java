package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟工具DTO
 * 迁移对应关系: Thrift struct MockTool
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockToolDTO {

  @Schema(description = "工具名称")
  private String name;

  @Schema(description = "模拟响应")
  private String mockResponse;
}
