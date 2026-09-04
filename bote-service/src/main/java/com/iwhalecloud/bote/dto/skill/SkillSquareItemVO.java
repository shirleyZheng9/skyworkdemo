package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SKILL广场列表项
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Getter
@Setter
@ToString
@Schema(description = "SKILL广场列表项")
public class SkillSquareItemVO {

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
  @Schema(description = "来源（如 square、zip；历史数据可能为 clawhub、skills.sh）")
  private String source;
  @Schema(description = "更新时间")
  protected Date updatedTime;
  @Schema(description = "技能拥有者用户ID")
  private Long ownerUserId;
  @Schema(description = "技能拥有者用户名")
  private String ownerUserName;
  @Schema(description = "是否已安装")
  private Boolean installed;
  @Schema(description = "agent skill 主键")
  private String agentSkillId;
}
