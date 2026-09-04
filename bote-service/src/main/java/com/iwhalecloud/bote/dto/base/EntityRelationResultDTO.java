package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 实体关系结果
 *
 * @author qian.sisheng
 * @since 2025-12-04
 */
@Setter
@Getter
@ToString
public class EntityRelationResultDTO {
  @Schema(description = "实体类型")
  private String entityType;
  @Schema(description = "实体ID")
  private Long entityId;
  @Schema(description = "引用元素(右侧-当前实体引用的元素)")
  private List<EntityRelationGroupDTO> linkGroups;
  @Schema(description = "被引元素(左侧-引用当前实体的元素)")
  private List<EntityRelationGroupDTO> backLinkGroups;
}
