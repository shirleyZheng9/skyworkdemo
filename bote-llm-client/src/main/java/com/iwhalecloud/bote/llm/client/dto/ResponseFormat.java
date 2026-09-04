package com.iwhalecloud.bote.llm.client.dto;

import com.iwhalecloud.bote.llm.client.consts.ResponseFormatType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 响应格式
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
public class ResponseFormat {
  /** 类型 */
  private ResponseFormatType type;

  public ResponseFormat() {
  }

  public ResponseFormat(ResponseFormatType type) {
    this.type = type;
  }

}
