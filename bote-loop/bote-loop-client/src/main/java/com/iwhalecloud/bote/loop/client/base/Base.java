package com.iwhalecloud.bote.loop.client.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础请求DTO
 * 对应Thrift: base.Base
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Base {

  /**
   * 日志ID
   * 对应Thrift字段: log_id
   */
  @Schema(description = "日志ID")
  private String logId;

  /**
   * 调用者
   * 对应Thrift字段: caller
   */
  @Schema(description = "调用者")
  private String caller;

  /**
   * 地址
   * 对应Thrift字段: addr
   */
  @Schema(description = "地址")
  private String addr;

  /**
   * 客户端
   * 对应Thrift字段: client
   */
  @Schema(description = "客户端")
  private String client;

  /**
   * 额外信息
   * 对应Thrift字段: extra
   */
  @Schema(description = "额外信息")
  private String extra;
}
