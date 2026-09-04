package com.iwhalecloud.bote.sandbox.dto.agentpool;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建沙箱响应
 *
 * @author bianjp
 * @since 2026-05-08
 */
@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class CreateSandboxResponse {
  /** 沙箱 ID */
  private Integer sandboxId;
  /** execd 接口地址 */
  private String execdEndpoint;
}
