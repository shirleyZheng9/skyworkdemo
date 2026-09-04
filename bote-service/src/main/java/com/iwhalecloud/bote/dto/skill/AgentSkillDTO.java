package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.entity.skill.AgentSkillEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill
 *
 * @author bianjp
 * @since 2026-02-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Agent Skill 传输对象")
public class AgentSkillDTO extends AgentSkillEntity {
  @Schema(description = "文件 ID")
  private Long fileId;
  @Schema(description = "文件名称")
  private String fileName;
  @Schema(description = "文件大小")
  private Long fileSize;
  @Schema(description = "更新人名称")
  private String updatorName;
  @Schema(description = "ai门户智能体是否启用")
  private Boolean enable;
  @Schema(description = "技能描述")
  private String skillDesc;
  @Schema(description = "技能版本")
  private String version;
  @Schema(description = "是否发布")
  private Boolean published;
  @Schema(description = "平台: 开发中心：developer、AI门户：aiPortal")
  private String platform;
}
