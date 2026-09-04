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
 * 机器人关联场景 Entity
 *
 * @author chen.linfa
 * @since 2025-04-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_BOT_SCENE_REL")
public class BotSceneRelEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long relId;

  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人 ID")
  private Long botId;

  @DiffField(name = "SCENE_ID")
  @Schema(description = "场景 ID")
  private Long sceneId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;

  @DiffField(name = "IS_DEFAULT")
  @Schema(description = "是否默认")
  private String isDefault;
}
