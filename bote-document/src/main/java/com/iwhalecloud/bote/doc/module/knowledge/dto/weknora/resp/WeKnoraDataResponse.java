package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * WeKnora API 通用响应包装
 *
 * @param <T> 数据类型
 * @author huangyunming
 * @since 2026-03-31
 */
@Getter
@Setter
public class WeKnoraDataResponse<T> {

  @JsonProperty("success")
  private Boolean success;

  @JsonProperty("message")
  private String message;

  @JsonProperty("data")
  private T data;
}
