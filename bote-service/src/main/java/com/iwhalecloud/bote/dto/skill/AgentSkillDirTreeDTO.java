package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill ：目录树节点（含目录下文件列表）
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
@Getter
@Setter
@ToString
@Schema(description = "Agent Skill 目录树节点")
public class AgentSkillDirTreeDTO {
 @Schema(description = "目录 ID")
 private Long dirId;
 @Schema(description = "目录名称")
 private String dirName;
 @Schema(description = "父目录 ID")
 private Long parentDirId;
 @Schema(description = "目录下文件")
 private List<AgentSkillFileDTO> files = new ArrayList<>();
 @Schema(description = "子目录节点")
 private List<AgentSkillDirTreeDTO> children = new ArrayList<>();
}

