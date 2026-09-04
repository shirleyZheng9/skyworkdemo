package com.iwhalecloud.bote.adapter.panzhi.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 实时语音转写结果类
 *
 * @author qian.sisheng
 * @since 2025-01-14
 */
@Getter
@Setter
@ToString
public class SpeechRecognitionDTO {

  /** 错误码，0表示成功 */
  private Integer errorCode;

  /** 错误信息 */
  private String errorMsg;

  /** 会话标记 */
  private String sid;

  /** 转写结果 */
  private Object result;

  /** 识别结束标记 */
  private Boolean endFlag;

  /** 获取纯文本结果（当result格式为plain时） */
  public String getPlainText() {
    if (result instanceof String) {
      return (String) result;
    }
    return null;
  }

  /**
   * 是否成功
   */
  public boolean isSuccess() {
    return errorCode != null && errorCode == 0;
  }

  /**
   * 是否有错误
   */
  public boolean hasError() {
    return !isSuccess();
  }
}
