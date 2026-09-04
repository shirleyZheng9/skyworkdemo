package com.iwhalecloud.bote.dto.asr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 实时语音识别结果 DTO
 *
 * @author qian.sisheng
 * @since 2026-01-28
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeAsrResultDTO {
  /** 识别文本 */
  private String text;
  /** 文本替换操作 replace:部分替换 replaceAll:全量替换 */
  private String opration;
  /** 是否最终结果 */
  private Boolean finaled;
}
