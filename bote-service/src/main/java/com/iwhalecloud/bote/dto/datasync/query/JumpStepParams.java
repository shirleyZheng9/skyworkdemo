package com.iwhalecloud.bote.dto.datasync.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 步骤化发布，手动环节流转入参
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
@Getter
@Setter
@ToString
public class JumpStepParams {
  /** 发布 ID */
  private Long publishId;
  /** 业务参数 */
  private Object object;
}
