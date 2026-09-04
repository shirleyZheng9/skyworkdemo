package com.iwhalecloud.bote.dto.model.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型推理入参
 *
 * @author chen.linfa
 * @since 2025-02-19
 */
@Getter
@Setter
@ToString
public class PredictRequest {
  /** 租户 ID */
  private String tenantId;
  /** 模型编码 */
  private String baseModelName;
  /** 模型 ID */
  private String fineTuningModelId;
  /** 用户消息 */
  private String text;
  /** 阈值（默认为 0） */
  private BigDecimal threshold;
  /** 上下文长度 */
  private Integer maxLength;
}
