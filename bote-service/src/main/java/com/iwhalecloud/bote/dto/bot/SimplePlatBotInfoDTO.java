package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 平台应用简单信息
 *
 * @author chen.linfa
 * @since 2025-09-18
 */
@Getter
@Setter
@ToString
public class SimplePlatBotInfoDTO {
  @Schema(description = "平台应用 ID")
  private Long platBotId;
  @Schema(description = "应用 ID")
  private Long botId;
  @Schema(description = "应用归属 ID")
  private Long ownerTenantId;
  @Schema(description = "应用名称")
  private String botName;
  @Schema(description = "应用类型")
  private String botType;
  @Schema(description = "访问链接")
  private String reqUrl;
  @Schema(description = "启用状态")
  private String status;
}
