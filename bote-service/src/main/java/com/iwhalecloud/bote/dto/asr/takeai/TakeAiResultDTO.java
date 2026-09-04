package com.iwhalecloud.bote.dto.asr.takeai;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TakeAi 识别结果项 DTO
 *
 * @author qian.sisheng
 * @since 2026-01-09
 */
@Getter
@Setter
@ToString
public class TakeAiResultDTO {
  /** 识别文本 */
  private String text;
  /** 开始时间 */
  private Integer start;
  /** 结束时间 */
  private Integer end;
}
