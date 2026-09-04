package com.iwhalecloud.bote.dto.chat.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author auto
 * @since 2024-09-19
 */
@Getter
@Setter
@ToString
@Schema(description = "场景意图识别参数")
public class SceneIntentParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "消息")
  private String message;
}
