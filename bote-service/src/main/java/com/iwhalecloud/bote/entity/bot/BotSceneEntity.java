package com.iwhalecloud.bote.entity.bot;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 场景
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_bot_scene")
public class BotSceneEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long sceneId;
  @Schema(description = "场景名称")
  @DiffField(name = "SCENE_NAME")
  @Size(max = 20, message = "智能体名称超过限定长度20")
  private String sceneName;
  @Schema(description = "场景描述")
  @DiffField(name = "SCENE_DESC")
  @Size(max = 400, message = "智能体描述超过限定长度400")
  private String sceneDesc;
  @Schema(description = "场景类型")
  @DiffField(name = "SCENE_TYPE")
  private String sceneType;
  @Schema(description = "场景编排内容")
  @DiffField(name = "SCENE_DSL")
  private String sceneDsl;
  @Schema(description = "流程图 JSON")
  @DiffField(name = "SCENE_GRAPH_JSON")
  private String sceneGraphJson;
  @Schema(description = "目录 ID")
  @DiffField(name = "CATALOG_ITEM_ID")
  private Long catalogItemId;
  @Schema(description = "场景状态: 下架：0, 上架：1")
  @DiffField(name = "SCENE_STATUS")
  private String sceneStatus;
  @Schema(description = "场景开场白")
  @DiffField(name = "PROLOGUE")
  private String prologue;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "SCENE_ICON")
  @Schema(description = "图标")
  private String sceneIcon;
  @DiffField(name = "FLOW_STEP_JSON")
  @Schema(description = "流程步骤 JSON")
  private String flowStepJson;
  @DiffField(name = "MODEL_ID")
  @Schema(description = "模型 ID")
  private Long modelId;
  @DiffField(name = "MODEL_CONFIG_JSON")
  @Schema(description = "模型配置 JSON")
  private String modelConfigJson;
  @DiffField(name = "LONG_TERM_MEMORY_ENABLED")
  @Schema(description = "是否启用长期记忆")
  private String longTermMemoryEnabled;
}
