package com.iwhalecloud.bote.dto.skill;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能安装记录视图对象
 *
 * @author skill-square
 * @since 2026-03-19
 */
@Getter
@Setter
@ToString
@Schema(description = "技能安装记录")
public class SkillInstallLogVO {

  @Schema(description = "记录ID")
  private Long id;

  @Schema(description = "技能ID")
  private Long skillId;

  @Schema(description = "技能编码")
  private String skillCode;

  @Schema(description = "技能名称")
  private String skillName;

  @Schema(description = "安装智能体ID")
  private Long botId;

  @Schema(description = "安装智能体名称")
  private String botName;

  @Schema(description = "租户ID")
  private Long tenantId;

  @Schema(description = "租户名称")
  private String tenantName;

  @Schema(description = "安装来源: square-广场, dialogue-对话")
  private String installSource;

  @Schema(description = "操作用户ID")
  private Long userId;

  @Schema(description = "操作用户名")
  private String userName;

  @Schema(description = "安装时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private OffsetDateTime installTime;
}
