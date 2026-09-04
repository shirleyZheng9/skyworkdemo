package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.dto.SystemReminder;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息扩展参数
 *
 * @author bianjp
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionMsgExtParamsDTO {
  /** 工具调用标识 */
  private String toolCallId;
  /** 附件 ID 列表 */
  private List<Long> fileIds;
  /** 用户消息中的参数(页面提交数据) */
  private Map<String, Object> params;
  /** 工具调用是否成功 */
  private Boolean success;
  /** 工具调用耗时 */
  private Long spentTime;
  /** 是否隐藏工具调用 */
  private Boolean hideToolCall;
  /** 事件类型(消息对应的 SSE 事件类型，比如 a2ui, page, pageFunc) */
  private String eventType;
  /** 事件数据 */
  private Object eventData;
  /** 系统提醒列表 */
  private List<SystemReminder> reminders;
}
