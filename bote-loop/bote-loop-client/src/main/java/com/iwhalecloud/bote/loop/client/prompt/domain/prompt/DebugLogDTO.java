package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试日志DTO
 * 迁移对应关系: Thrift struct DebugLog
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugLogDTO {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "提示词键")
  private String promptKey;

  @Schema(description = "版本")
  private String version;

  @Schema(description = "输入令牌数")
  private Long inputTokens;

  @Schema(description = "输出令牌数")
  private Long outputTokens;

  @Schema(description = "耗时（毫秒）")
  private Long costMs;

  @Schema(description = "状态码")
  private Integer statusCode;

  @Schema(description = "调试者")
  private String debuggedBy;

  @Schema(description = "调试ID")
  private Long debugId;

  @Schema(description = "调试步骤")
  private Integer debugStep;

  @Schema(description = "开始时间")
  private Long startedAt;

  @Schema(description = "结束时间")
  private Long endedAt;
}
