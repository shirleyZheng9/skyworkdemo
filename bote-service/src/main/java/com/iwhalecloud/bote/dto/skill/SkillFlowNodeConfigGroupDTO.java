package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能流程节点配置分组 DTO（用于根节点 tip 字段）
 *
 * @author lizuyin
 * @since 2025-12-26
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SkillFlowNodeConfigGroupDTO {
  @Schema(description = "分组名称")
  private String groupName;
  @Schema(description = "分组编码")
  private String groupCode;
}

