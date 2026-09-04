package com.iwhalecloud.bote.dto.dashboard;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 每日对话消息量数据库聚合结果。
 *
 * @author zhengxueli
 * @since 2026-08-31
 */
@Getter
@Setter
@ToString
public class ConversationMessageTrendCountDTO {

  /** yyyy-MM-dd 格式的统计日期。 */
  private String statDate;

  /** 当日消息量。 */
  private Long messageCount;
}
