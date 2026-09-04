package com.iwhalecloud.bote.dto.portal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 外系统Session信息
 *
 * @author auto
 * @since 2024-10-14
 */
@Getter
@Setter
@ToString
@Builder
public class ExternalSystemSession {
  @Schema(description = "系统编码")
  private String systemCode;
  @Schema(description = "会话 ID")
  private String sessionId;
}
