package com.iwhalecloud.bote.llm.client.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型配置信息
 *
 * @author bianjp
 * @since 2025-11-10
 */
@Getter
@Setter
@ToString
@Builder
public class ModelConfigInfoDTO {
  /** 模型 ID */
  private Long modelId;
  /** 租户 ID */
  private Long tenantId;
  /** 模型名称 */
  private String modelName;
  /** 模型编码 */
  private String modelCode;
  /** 协议类型 */
  private String protocolType;
  /** 访问地址 */
  private String accessUrl;
}
