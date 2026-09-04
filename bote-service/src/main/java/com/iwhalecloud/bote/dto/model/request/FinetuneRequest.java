package com.iwhalecloud.bote.dto.model.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型微调入参
 *
 * @author chen.linfa
 * @since 2025-02-19
 */
@Getter
@Setter
@ToString
public class FinetuneRequest {
  /** 模型 ID */
  private String publishId;
  /** 租户 ID */
  private String tenantId;
  /** 模型名称 */
  private String baseModelName;

  /** 文件 ID */
  private Long fileId;
  /** 文件存储类型 */
  private String fileServer;
  /** 文件存储路径 */
  private String filePath;

  /** 循环次数 */
  private Integer numTrainEpochs;
  /** 学习率 */
  private BigDecimal learningRate;
  /** 批量大小 */
  private Integer batchSize;
  /** 长度 */
  private Integer maxLength;
  /** 学习率预热比例 */
  private BigDecimal weightDecay;
  /** 阈值，后端根据语料计算出合理值 */
  private BigDecimal threshold;
}
