package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt结果数据DTO
 * 对应Thrift: PromptResultData
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Prompt结果数据DTO")
public class PromptResultData {

  /**
   * 结果项列表
   * 对应Thrift字段: items
   */
  @Schema(description = "结果项列表")
  private List<PromptResult> items;
}
