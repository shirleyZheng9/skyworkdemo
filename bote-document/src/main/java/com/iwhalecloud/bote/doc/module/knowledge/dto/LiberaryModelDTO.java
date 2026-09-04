package com.iwhalecloud.bote.doc.module.knowledge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author qian.sisheng
 * @since 2026-04-09
 */
@Getter
@Setter
@ToString
public class LiberaryModelDTO {

  private String modelName;
  private String modelCode;
  private Long modelId;
  private String accessUrl;
  private String accessKey;
}
