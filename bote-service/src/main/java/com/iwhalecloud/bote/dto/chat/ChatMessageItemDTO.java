package com.iwhalecloud.bote.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息条目
 *
 * <p>用于会话查询的分页查询结果</p>
 *
 * @author bianjp
 * @since 2025-01-08
 */
@Getter
@Setter
@ToString
@Schema(description = "会话消息条目")
public class ChatMessageItemDTO {
  @Schema(description = "消息 ID")
  private Long msgId;
  @Schema(description = "消息类型")
  private String msgType;
  @Schema(description = "消息内容")
  private String msgText;
  @Schema(description = "开始时间")
  private Date beginTime;
  @Schema(description = "会话 ID")
  private Long sessionId;
  @Schema(description = "处理状态(S: 成功, F: 失败)")
  private String msgStatus;
  @Schema(description = "机器人 ID")
  private String botId;
  @Schema(description = "机器人名称")
  private String botName;
  @Schema(description = "机器人归属的租户 ID (可能与当前租户不同)")
  private Long botTenantId;
  @Schema(description = "场景 ID")
  private Long sceneId;
  @Schema(description = "场景名称")
  private String sceneName;
  @Schema(description = "场景类型")
  private String sceneType;
  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "用户名称")
  private String userName;
  @Schema(description = "用户编码")
  private String userCode;
}
