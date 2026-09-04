package com.iwhalecloud.bote.entity.bot;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模板智能体 Entity
 *
 * @author auto
 * @since 2025-06-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_plat_scene_info")
public class PlatSceneInfoEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long platSceneId;

  @DiffField(name = "SCENE_ID")
  @Schema(description = "智能体ID")
  private Long sceneId;

  @DiffField(name = "SORT_ORDER")
  @Schema(description = "排序值")
  private Integer sortOrder;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "模板分类ID")
  private Long catalogItemId;
}
