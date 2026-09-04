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
 * 会话消息 Entity
 *
 * @author auto
 * @since 2024-10-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_bot_session_msg")
public class SessionMsgEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long msgId;
  @DiffField(name = "SESSION_ID")
  @Schema(description = "会话 ID")
  private Long sessionId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "应用 ID")
  private Long botId;
  @DiffField(name = "TRANSACTION_ID")
  @Schema(description = "事务 ID")
  private Long transactionId;
  @DiffField(name = "SCENE_ID")
  @Schema(description = "场景 ID")
  private Long sceneId;
  @DiffField(name = "CONTEXT_ID")
  @Schema(description = "上下文 ID")
  private String contextId;
  @DiffField(name = "MSG_TYPE")
  @Schema(description = "消息类型(input, point)")
  private String msgType;
  @DiffField(name = "ROLE")
  @Schema(description = "角色(system:系统;user:用户;assistant:助手;tool:工具)")
  private String role;
  @DiffField(name = "LIKE_TYPE")
  @Schema(description = "点赞类型(1:点赞;2:点踩;3:不予置评;)")
  private String likeType;
  @DiffField(name = "BEGIN_TIME")
  @Schema(description = "开始时间")
  private Date beginTime;
  @DiffField(name = "END_TIME")
  @Schema(description = "结束时间")
  private Date endTime;
  @Schema(description = "消息状态")
  private String msgStatus;
  @Schema(description = "是否用作记忆(T/F)")
  protected String memorized;
  @DiffField(name = "SORT")
  @Schema(description = "排序号")
  private Integer sort;
  @DiffField(name = "REF_MSG_ID")
  @Schema(description = "关联的消息 ID")
  private Long refMsgId;
  @DiffField(name = "PLAN_ID")
  @Schema(description = "计划 ID")
  private Long planId;
}
