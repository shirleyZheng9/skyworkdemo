package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 资源关联 Entity
 *
 * @author auto
 * @since 2025-04-09
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ResourceElementEntity extends BaseEntity {

  @Schema(description = "主键")
  private Long resourceElementId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "资源类型")
  private String resourceType;
  @Schema(description = "资源 ID")
  private Long resourceId;
  @Schema(description = "元素类型")
  private String elementType;
  @Schema(description = "元素 ID")
  private Long elementId;
}
