package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体简要信息（占位用）
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "智能体简要信息")
public class ClawBotItemVO {

  @Schema(description = "智能体ID")
  private Long botId;
  @Schema(description = "智能体名称")
  private String botName;
}
