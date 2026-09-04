package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能流程节点项 DTO
 *
 * @author lizuyin
 * @since 2025-12-26
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SkillFlowNodeItemDTO {
  @Schema(description = "节点 key")
  private String key;
  @Schema(description = "节点名称")
  private String name;
  @Schema(description = "节点描述")
  private String desc;
  @Schema(description = "流程类型列表")
  private List<String> flowTypes;
  @Schema(description = "场景类型列表")
  private List<String> sceneTypes;
}

