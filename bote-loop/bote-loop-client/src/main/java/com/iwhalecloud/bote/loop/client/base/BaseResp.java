package com.iwhalecloud.bote.loop.client.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础响应DTO
 * 对应Thrift: base.BaseResp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseResp {

  /**
   * 状态消息
   * 对应Thrift字段: status_message
   */
  @Schema(description = "状态消息")
  private String statusMessage;

  /**
   * 状态码
   * 对应Thrift字段: status_code
   */
  @Schema(description = "状态码")
  private Integer statusCode;

  /**
   * 额外信息
   * 对应Thrift字段: extra
   */
  @Schema(description = "额外信息")
  private String extra;
}
