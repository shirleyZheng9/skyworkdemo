package com.iwhalecloud.bote.dto.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微调模型状态变更入参
 *
 * @author chen.linfa
 * @since 2025-02-19
 */
@Getter
@Setter
@ToString
public class CommonRequest {
  /** 模型 ID 或 评测 ID */
  private String publishId;
  /** 租户 ID */
  private String tenantId;
  /** 模型编码 */
  private String baseModelName;
  /** 模型 ID */
  private String fineTuningModelId;
  /** 文件存储类型 */
  private String fileServer;
}
