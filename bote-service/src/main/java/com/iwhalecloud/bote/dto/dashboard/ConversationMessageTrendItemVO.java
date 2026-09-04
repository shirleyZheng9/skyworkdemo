package com.iwhalecloud.bote.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 每日对话消息量趋势项。
 *
 * @author zhengxueli
 * @since 2026-08-31
 */
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "每日对话消息量趋势项")
public class ConversationMessageTrendItemVO {

  @Schema(description = "统计日期", example = "2026-08-31")
  private final LocalDate date;

  @Schema(description = "用户对话消息量", example = "1920")
  private final Long messageCount;
}
