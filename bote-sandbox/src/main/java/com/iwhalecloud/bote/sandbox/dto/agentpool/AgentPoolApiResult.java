package com.iwhalecloud.bote.sandbox.dto.agentpool;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Pool 的 API 响应结果
 *
 * @author bianjp
 * @since 2026-01-14
 */
@Getter
@Setter
@ToString
public final class AgentPoolApiResult<T> {
  /** 响应编码, 200 表示成功 */
  private Integer code;
  /** 响应描述 */
  private String message;
  /** 响应数据 */
  private T data;

  /**
   * 是否成功
   */
  @JsonIgnore
  public boolean isSuccess() {
    return this.code != null && this.code == 200;
  }
}
