package com.iwhalecloud.bote.llm.client.consts;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 图片细节级别
 *
 * @author bianjp
 * @since 2024-08-01
 */
public enum ImageDetail {
  @JsonProperty("low")
  LOW,
  @JsonProperty("high")
  HIGH,
  @JsonProperty("auto")
  AUTO
}
