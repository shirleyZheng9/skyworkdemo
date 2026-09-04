package com.iwhalecloud.bote.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话分组
 *
 * @author chen.linfa
 * @since 2025-06-05
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话分组")
public class SimpleSessionGroupDTO {

  @Schema(description = "是否应用")
  private Boolean isBot;
  @Schema(description = "应用归属租户 ID")
  private Long tenantId;
  @Schema(description = "应用 ID")
  private Long botId;
  @Schema(description = "应用名称")
  private String botName;
  @Schema(description = "广场应用 ID")
  private Long platBotId;
  @Schema(description = "广场应用类型")
  private String platBotType;
  @Schema(description = "广场应用访问地址")
  private String platReqUrl;
  @Schema(description = "会话 ID")
  private Long sessionId;
  @Schema(description = "会话标题")
  private String sessionTitle;
  @Schema(description = "更新时间")
  private Date updatedTime;
  @Schema(description = "最后一条消息内容")
  private String lastMsgText;
  @Schema(description = "对话应用配置")
  private SimpleBotCfgDTO botCfg;
  @Schema(description = "用于区分已对话过的应用，是否可用。已下架应用前端需要特殊处理")
  private Boolean enable;
  @Schema(description = "是否为 BoteClaw")
  private String isBoteClaw;
  @Schema(description = "是否平台发布：T:是，F:否")
  private String isPlatformPublish;
}
