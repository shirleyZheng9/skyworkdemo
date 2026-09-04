package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能安装请求
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@Schema(description = "技能安装请求")
public class InstallRequest {

  @NotNull(message = "skillId 不能为空")
  @Schema(description = "广场技能ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long skillId;

  @Schema(description = "智能体ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long botId;

  @Schema(description = "安装来源: square-广场, dialogue-对话", example = "square")
  private String installSource;

  @Schema(description = "租户ID")
  @NotNull(message = "租户id 不能为空")
  private Long tenantId;
}
