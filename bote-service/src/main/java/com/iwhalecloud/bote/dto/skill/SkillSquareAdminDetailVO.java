package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SKILL广场管理端技能详情（含 onlineStatus）
 *
 * @author skill-square
 * @since 2026-03-19
 */
@Getter
@Setter
@ToString
@Schema(description = "SKILL广场管理端技能详情")
public class SkillSquareAdminDetailVO extends SkillSquareDetailVO {

  @Schema(description = "上架状态: T-上架, F-下架")
  private String onlineStatus;
}
