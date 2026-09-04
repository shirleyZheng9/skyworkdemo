package com.iwhalecloud.bote.dto;

import com.iwhalecloud.bote.common.enums.SystemReminderType;

/**
 * 系统提醒
 *
 * @param type 类型
 * @param text 文本(包括 {@code <system-reminder>} 标签)
 * @author bianjp
 * @since 2026-04-13
 */
public record SystemReminder(SystemReminderType type, String text) {
}
