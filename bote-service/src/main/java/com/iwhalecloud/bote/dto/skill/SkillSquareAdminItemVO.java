package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SKILL广场管理端列表项（含 onlineStatus）
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@Schema(description = "SKILL广场管理端列表项")
public class SkillSquareAdminItemVO extends SkillSquareItemVO {

  @Schema(description = "上架状态: T-上架, F-下架")
  private String onlineStatus;
}
