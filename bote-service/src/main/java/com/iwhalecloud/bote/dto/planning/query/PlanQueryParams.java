package com.iwhalecloud.bote.dto.planning.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 计划参数
 *
 * @author chen.linfa
 * @since 2025-05-14
 */
@Getter
@Setter
@ToString
public class PlanQueryParams {

  @Schema(description = "应用 ID")
  private Long botId;

  @Schema(description = "会话 ID")
  private Long sessionId;

  @Schema(description = "用户消息")
  private String userMessage;
}
