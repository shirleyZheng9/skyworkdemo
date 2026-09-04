package com.iwhalecloud.bote.dto.bot.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景提示词参数
 *
 * @author chen.linfa
 * @since 2024-08-27
 */
@Getter
@Setter
@ToString
@Schema(description = "场景提示词参数")
public class BotScenePromptParams {
  @Schema(description = "提示词")
  private String prompt;
  @Schema(description = "租户 ID")
  private Long tenantId;
}
