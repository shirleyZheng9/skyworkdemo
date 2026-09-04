package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能流程节点配置 Entity
 *
 * @author lizuyin
 * @since 2025-12-26
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_flow_node_cfg")
public class SkillFlowNodeCfgEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "CODE")
  @Schema(description = "节点编码")
  private String code;
  @DiffField(name = "NAME")
  @Schema(description = "节点名称")
  private String name;
  @DiffField(name = "TIP")
  @Schema(description = "节点描述")
  private String tip;
  @DiffField(name = "SCENE_TYPES")
  @Schema(description = "场景类型")
  private String sceneTypes;
  @DiffField(name = "FLOW_TYPES")
  @Schema(description = "流程类型")
  private String flowTypes;
  @DiffField(name = "SORTBY")
  @Schema(description = "排序")
  private Integer sortby;
  @DiffField(name = "IS_ROOT")
  @Schema(description = "是否根节点")
  private String isRoot;
  @DiffField(name = "GROUP_CODE")
  @Schema(description = "分组编码")
  private String groupCode;
  @DiffField(name = "UPDATED_TIME")
  @Schema(description = "更新时间")
  private java.util.Date updatedTime;
}

