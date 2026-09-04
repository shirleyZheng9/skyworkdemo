package com.iwhalecloud.bote.dto.intent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图匹配项
 *
 * @author bianjp
 * @since 2024-12-23
 */
@Getter
@Setter
@ToString
public class IntentionMatchItemDTO {
  /** 意图问句 ID */
  private Long questionId;
  /** 评分(范围为 0~1, 值越大表示匹配度越高) */
  private Float score;
}
