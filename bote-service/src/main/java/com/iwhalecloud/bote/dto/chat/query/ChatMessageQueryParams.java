package com.iwhalecloud.bote.dto.chat.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息查询条件
 *
 * @author bianjp
 * @since 2025-01-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "会话消息查询条件")
public class ChatMessageQueryParams extends PagingQueryParams {
  @Schema(description = "租户 ID", requiredMode = RequiredMode.REQUIRED)
  private Long tenantId;
  @Schema(description = "应用 ID")
  private Long botId;
  @Schema(description = "场景 ID")
  private Long sceneId;
  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "最小会话时间")
  private Date minBeginTime;
  @Schema(description = "最大会话时间")
  private Date maxBeginTime;
  @Schema(description = "消息类型")
  private String msgType;
  @Schema(description = "处理状态(S: 成功, F: 失败)")
  private String msgStatus;
  @Schema(description = "消息内容关键字（模糊查询）")
  private String keyword;
  @Schema(description = "会话 ID")
  private Long sessionId;
  @Schema(description = "最后消息的序号，用于从指定消息后收集数据")
  private Integer lastMsgSort;
  @Schema(description = "点踩状态")
  private String likeType;
}
