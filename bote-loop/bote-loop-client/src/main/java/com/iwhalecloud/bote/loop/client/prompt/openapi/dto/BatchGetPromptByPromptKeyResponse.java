package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取Prompt响应DTO
 * 对应Thrift: BatchGetPromptByPromptKeyResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取Prompt响应DTO")
public class BatchGetPromptByPromptKeyResponse {

  /**
   * 响应码
   * 对应Thrift字段: code
   */
  @Schema(description = "响应码")
  private Integer code;

  /**
   * 响应消息
   * 对应Thrift字段: msg
   */
  @Schema(description = "响应消息")
  private String msg;

  /**
   * 响应数据
   * 对应Thrift字段: data
   */
  @Schema(description = "响应数据")
  private PromptResultData data;

  /**
   * 基础响应信息
   * 对应Thrift字段: BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
