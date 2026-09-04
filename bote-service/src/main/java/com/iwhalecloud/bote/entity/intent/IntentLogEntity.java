package com.iwhalecloud.bote.entity.intent;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图识别日志 Entity
 *
 * @author auto
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_CHAT_INTENTION_LOG")
public class IntentLogEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long logId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人ID")
  private Long botId;
  @DiffField(name = "SCENE_ID")
  @Schema(description = "场景ID")
  private Long sceneId;
  @DiffField(name = "CONTENT")
  @Schema(description = "意图语句")
  private String content;
  @DiffField(name = "MARK_STATUS")
  @Schema(description = "意图日志状态：T 已标记  F 未标记")
  private String markStatus;
}
