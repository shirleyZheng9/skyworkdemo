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
 * 机器人收藏 Entity
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_bot_favor")
public class BotFavorEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long favorId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人 ID")
  private Long botId;
  @DiffField(name = "USER_ID")
  @Schema(description = "用户 ID")
  private Long userId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
}
