package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能流程节点配置响应 DTO
 *
 * @author lizuyin
 * @since 2025-12-26
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SkillFlowNodeConfigResponseDTO {
  @Schema(description = "分组编码")
  private String groupId;
  @Schema(description = "分组名称")
  private String groupName;
  @Schema(description = "节点列表")
  private List<SkillFlowNodeItemDTO> items;
}

