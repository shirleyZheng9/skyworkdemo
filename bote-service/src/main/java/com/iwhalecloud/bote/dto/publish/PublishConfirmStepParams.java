package com.iwhalecloud.bote.dto.publish;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 发布确认步骤参数
 *
 * @author qian.sisheng
 * @since 2026/02/09
 */
@Getter
@Setter
@ToString
public class PublishConfirmStepParams {
  @Schema(description = "网关ID")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Long gatewayId;
  @Schema(description = "租户ID")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Long tenantId;
  @Schema(description = "网关URL")
  private String gatewayUrl;
  @Schema(description = "网关Token")
  private String gatewayToken;
  @Schema(description = "记录ID")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Long publishId;
}
