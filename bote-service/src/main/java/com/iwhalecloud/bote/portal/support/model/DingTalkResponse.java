package com.iwhalecloud.bote.portal.support.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Aiqing
 * @since 2025/6/5
 */
@Data
public class DingTalkResponse<T> {

  @JsonProperty("errcode")
  private String errCode;
  @JsonProperty("errMsg")
  private String errMsg;
  private T result;

  @JsonProperty("request_id")
  private String requestId;

  public boolean isSuccess() {
    return Objects.equals("0", this.errCode);
  }
}
