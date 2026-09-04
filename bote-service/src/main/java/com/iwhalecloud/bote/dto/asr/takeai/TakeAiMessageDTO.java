package com.iwhalecloud.bote.dto.asr.takeai;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TakeAi 完整消息 DTO
 *
 * @author qian.sisheng
 * @since 2026-01-09
 */
@Getter
@Setter
@ToString
public class TakeAiMessageDTO {
  /** 消息头 */
  private TakeAiHeaderDTO header;
  /** 消息载荷 */
  private TakeAiPayloadDTO payload;
}
