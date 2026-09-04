package com.iwhalecloud.bote.dto.publish;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 个人微信（ClawBot）确认扫码登录请求体。
 *
 * @author lizuyin
 * @since 2026-04-13
 */
@Getter
@Setter
@ToString
public class WeClawBotConfirmLoginRequest {

  @Schema(description = "二维码 qrcode 字符串")
  @JsonProperty("qrcode")
  private String qrcode;
  @Schema(description = "最大等待秒数，默认 300")
  private Integer maxWaitSeconds;
  @Schema(description = "轮询间隔毫秒，默认 1500")
  private Integer pollIntervalMs;
}
