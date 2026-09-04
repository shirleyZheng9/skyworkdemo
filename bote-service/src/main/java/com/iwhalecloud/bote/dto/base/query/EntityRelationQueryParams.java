package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 血缘分组详情查询参数DTO
 *
 * @author qian.sisheng
 * @since 2025-12-05
 */
@Getter
@Setter
@ToString
public class EntityRelationQueryParams {
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "实体类型")
  private String entityType;
  @Schema(description = "按类型分组的实体ID映射")
  private List<Long> entityIds;
  @Schema(description = "实体ID")
  private Long entityId;
  @Schema(description = "按类型分Groups的实体ID映射")
  private List<EntityRelationQueryParams> entities;
}
