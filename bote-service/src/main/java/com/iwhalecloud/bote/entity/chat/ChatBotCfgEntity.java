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
 * 对话应用配置 Entity
 *
 * @author chen.linfa
 * @since 2025-09-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_chat_bot_cfg")
public class ChatBotCfgEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间 ID")
  private Long spaceId;

  @DiffField(name = "BOT_ID")
  @Schema(description = "应用 ID")
  private Long botId;

  @DiffField(name = "BOT_TENANT_ID")
  @Schema(description = "应用归属租户 ID")
  private Long botTenantId;

  @DiffField(name = "USER_ID")
  @Schema(description = "用户 ID")
  private Long userId;

  @DiffField(name = "ACTION_TYPE")
  @Schema(description = "动作类型")
  private String actionType;
}
