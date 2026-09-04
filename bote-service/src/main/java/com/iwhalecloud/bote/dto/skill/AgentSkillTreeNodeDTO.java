package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill 树节点（目录/文件统一结构）
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@ToString
@Schema(description = "Agent Skill 树节点")
public class AgentSkillTreeNodeDTO {

  @Schema(description = "节点 ID（目录使用 dirId，文件使用 skillFileId）")
  private Long nodeId;

  @Schema(description = "父节点 ID（根节点为 -1）")
  private Long parentId;

  @Schema(description = "节点名称（目录名/文件名）")
  private String nodeName;

  @Schema(description = "节点类型：dir/file")
  private String nodeType;

  @Schema(description = "文件类型（仅文件节点有值）")
  private String fileType;

  @Schema(description = "是否可在线编辑（仅文件节点有值）")
  private Boolean textEditable;

  @Schema(description = "子节点")
  private List<AgentSkillTreeNodeDTO> children = new ArrayList<>();
}

