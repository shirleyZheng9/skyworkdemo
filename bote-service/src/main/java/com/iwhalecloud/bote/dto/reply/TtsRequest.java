package com.iwhalecloud.bote.dto.reply;

import org.springframework.lang.Nullable;

/**
 * 文字转语音请求对象
 *
 * @param tts 文本
 * @param audio 音色，支持 man, sweetGirl, 默认为 man
 * @author bianjp
 * @since 2026-01-13
 */
public record TtsRequest(String tts, @Nullable String audio, @Nullable String speedFactor) {
}
