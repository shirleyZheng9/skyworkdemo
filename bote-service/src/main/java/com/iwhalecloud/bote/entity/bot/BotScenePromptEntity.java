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
 * 场景提示词
 *
 * @author chen.linfa
 * @since 2024-08-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_bot_scene_prompt")
public class BotScenePromptEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long scenePromptId;
  @Schema(description = "场景 ID")
  @DiffField(name = "SCENE_ID", parent = true)
  private Long sceneId;
  @Schema(description = "提示词 ID")
  @DiffField(name = "prompt_id")
  private Long promptId;
  @Schema(description = "提示词")
  @DiffField(name = "scene_prompt")
  private String scenePrompt;
  @Schema(description = "提示词类型")
  @DiffField(name = "PROMPT_TYPE")
  private String promptType;
  @Schema(description = "租户 ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
}
