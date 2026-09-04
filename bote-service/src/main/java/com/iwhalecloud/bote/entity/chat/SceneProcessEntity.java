package com.iwhalecloud.bote.entity.chat;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话场景进度 Entity
 *
 * @author chen.linfa
 * @since 2024-12-17
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_chat_scene_process")
public class SceneProcessEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField(name = "SESSION_ID")
  @Schema(description = "会话 ID")
  private Long sessionId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;

  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人 ID")
  private Long botId;

  @DiffField(name = "SCENE_ID")
  @Schema(description = "场景 ID")
  private Long sceneId;

  @DiffField(name = "SCENE_NAME")
  @Schema(description = "场景名称")
  private String sceneName;

  @DiffField(name = "CONTEXT_ID")
  @Schema(description = "场景上下文 ID")
  private String contextId;

  @DiffField(name = "CHAT_STATUS")
  @Schema(description = "对话场景状态，1 运行中；2 已完成")
  private String chatStatus;
}
