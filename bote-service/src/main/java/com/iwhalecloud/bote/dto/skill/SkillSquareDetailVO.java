package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SKILL广场技能详情
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@Schema(description = "SKILL广场技能详情")
public class SkillSquareDetailVO {

  @Schema(description = "技能ID")
  private Long skillId;
  @Schema(description = "技能编码")
  private String skillCode;
  @Schema(description = "技能名称")
  private String skillName;
  @Schema(description = "技能简介")
  private String skillDesc;
  @Schema(description = "技能类型: platform/community")
  private String skillType;
  @Schema(description = "标签JSON")
  private String tags;
  @Schema(description = "版本号")
  private String version;
  @Schema(description = "安装次数")
  private Integer installCount;
  @Schema(description = "SKILL.md 内容（readmeContent）")
  private String readmeContent;
  @Schema(description = "来源: square/skills.sh/clawhub")
  private String source;
}
