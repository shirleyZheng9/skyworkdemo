package com.iwhalecloud.bote.entity.chat;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话 Entity
 *
 * @author auto
 * @since 2024-10-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_bot_session")
public class SessionEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long sessionId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人 ID")
  private Long botId;
  @DiffField(name = "BOT_TENANT_ID")
  @Schema(description = "应用归属租户 ID")
  private Long botTenantId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "EXT_SYSTEM_ID")
  @Schema(description = "外系统 ID")
  private Long extSystemId;
  @DiffField(name = "PLAT_BOT_ID")
  @Schema(description = "平台应用ID")
  private Long platBotId;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间 ID")
  private Long spaceId;
  @DiffField(name = "SESSION_TITLE")
  @Schema(description = "会话标题")
  private String sessionTitle;
  @DiffField(name = "PROLOGUE")
  @Schema(description = "开场白")
  private String prologue;
  @DiffField(name = "BEGIN_TIME")
  @Schema(description = "开始时间")
  private Date beginTime;
  @DiffField(name = "END_TIME")
  @Schema(description = "结束时间")
  private Date endTime;
  @DiffField(name = "IS_TEST")
  @Schema(description = "是否调试")
  private String isTest;
}
