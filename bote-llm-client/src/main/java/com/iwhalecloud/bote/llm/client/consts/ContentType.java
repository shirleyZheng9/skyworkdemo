package com.iwhalecloud.bote.llm.client.consts;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 内容类型
 *
 * @author bianjp
 * @since 2024-08-01
 */
public enum ContentType {
  /** 文本 */
  @JsonProperty("text")
  TEXT,
  /** 图片 */
  @JsonProperty("image_url")
  IMAGE_URL
}
