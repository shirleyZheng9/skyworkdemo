package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * API 服务测试请求
 */
@Getter
@Setter
@ToString
@Schema(description = "API 服务测试请求")
public class ServiceTestRequest {
  @Schema(description = "服务")
  private SimpleServiceDTO service;
  @Schema(description = "请求参数")
  private ApiServiceParams params;
  @Schema(description = "客户端 ID(流式接口中断请求使用)")
  private String clientId;
}
