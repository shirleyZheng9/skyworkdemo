package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 实体关系分组
 *
 * @author qian.sisheng
 * @since 2025-12-04
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class EntityRelationGroupDTO {
  @Schema(description = "分组名称")
  private String groupName;
  @Schema(description = "分组编码")
  private String groupCode;
  @Schema(description = "元素数量(预览模式)")
  private Integer count;
  @Schema(description = "元素ID列表")
  private List<String> entityIds;
  @Schema(description = "引用的元素(详情模式)")
  private List<EntityInfoDTO> relationList;
  @Schema(description = "子分组列表(用于技能等嵌套分组)")
  private List<EntityRelationGroupDTO> subGroups;
}
