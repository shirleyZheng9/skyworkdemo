package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 管理端技能状态更新请求
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@Schema(description = "管理端技能状态更新请求")
public class AdminUpdateStatusRequest {

  @NotNull(message = "skillId 不能为空")
  @Schema(description = "技能ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long skillId;

  @NotBlank(message = "action 不能为空")
  @Schema(description = "操作: enable-上架, disable-下架, delete-软删除", requiredMode = Schema.RequiredMode.REQUIRED)
  private String action;
}
