package com.iwhalecloud.bote.dto.asr;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 实时语音识别请求 DTO
 *
 * @author qian.sisheng
 * @since 2026-01-10
 */
@Getter
@Setter
@ToString
public class RealtimeAsrRequestDTO {
  /** 操作类型: start, stop */
  private String action;
  /** Base64 编码的音频数据 (当 action 为空时有效) */
  private String data;
  /** 开始时间戳 */
  private Long start;
  /** 结束时间戳 */
  private Long end;
}
