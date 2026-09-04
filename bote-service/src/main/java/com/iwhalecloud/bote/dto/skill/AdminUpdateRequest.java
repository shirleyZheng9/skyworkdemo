package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 管理端单技能编辑请求（packageFile 由 Controller 单独接收）
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@Schema(description = "管理端单技能编辑请求")
public class AdminUpdateRequest {

  @NotNull(message = "skillId 不能为空")
  @Schema(description = "技能ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long skillId;

  @Schema(description = "技能名称（可选）")
  private String skillName;

  @Schema(description = "技能描述（可选）")
  private String skillDesc;
}
