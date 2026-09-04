package com.iwhalecloud.bote.dto.model.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型评测入参
 *
 * @author chen.linfa
 * @since 2025-02-19
 */
@Getter
@Setter
@ToString
public class EvalRequest {
  /** 评测 ID */
  private String publishId;
  /** 租户 ID */
  private String tenantId;
  /** 模型编码 */
  private String baseModelName;
  /** 模型 ID */
  private String fineTuningModelId;

  /** 文件存储类型 */
  private String fileServer;
  /** 文件存储路径 */
  private String filePath;

  /** 阈值（默认为 0） */
  private BigDecimal threshold;
  /** 问题长度 */
  private Integer maxLength;
  /**  */
  private Integer batchSize;
}
