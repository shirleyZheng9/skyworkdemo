package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill 一键发布结果
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
@Getter
@Setter
@ToString
@Schema(description = "Agent Skill 一键发布结果")
public class AgentSkillPublishResultDTO {
  @Schema(description = "文件信息ID")
  private Long fileInfoId;
  @Schema(description = "文件名")
  private String zipFileName;
  @Schema(description = "发布时间")
  private Date publishedTime;
}
