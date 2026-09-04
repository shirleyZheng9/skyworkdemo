package com.iwhalecloud.bote.dto.asr;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语音识别信息
 *
 * @author qian.sisheng
 * @since 2026-01-12
 */
@Getter
@Setter
@ToString
public class VideoProviderInfoDTO {
  /** 是否开启实时识别 */
  private Boolean realtimeEnabled;
  /** 是否支持实时识别 */
  private Boolean supportRealtime;
  /** 语音识别类型 */
  private String provider;
}
