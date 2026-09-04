package com.iwhalecloud.bote.dto.asr.takeai;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TakeAi 音频数据 DTO
 *
 * @author qian.sisheng
 * @since 2026-01-09
 */
@Getter
@Setter
@ToString
public class TakeAiAudioDTO {
  /** Base64 编码的音频数据 */
  private String data;
  /** 开始时间戳 */
  private Integer start;
  /** 结束时间戳 */
  private Integer end;
}
