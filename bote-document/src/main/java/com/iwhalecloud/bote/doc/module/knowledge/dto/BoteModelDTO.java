package com.iwhalecloud.bote.doc.module.knowledge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型信息 DTO
 *
 * @author qian.sisheng
 * @since 2026-04-10
 */
@Setter
@Getter
@ToString
public class BoteModelDTO {
  /** 模型ID */
  private Long modelId;
  /** 模型编码 */
  private String modelCode;
  /** 模型名称 */
  private String modelName;
  /** 模型描述 */
  private String modelDesc;
  /** 模型URL */
  private String accessUrl;
  /** 密钥 */
  private String accessKey;
  /** 模型类型 */
  private String modelType;
}
