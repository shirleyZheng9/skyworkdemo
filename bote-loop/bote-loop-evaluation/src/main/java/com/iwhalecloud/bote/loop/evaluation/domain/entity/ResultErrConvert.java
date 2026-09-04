package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * 结果错误转换配置
 */
@Getter
@Setter
public class ResultErrConvert {

  private String matchedText;
  private int toErrCode;
  private String toErrMsg;
  private Boolean asDefault;

  public ConvertErrMsgResult convertErrMsg(String msg) {
    if (msg == null || msg.isEmpty()) {
      return new ConvertErrMsgResult(false, "");
    }
    if (toErrCode > 0) {
      return new ConvertErrMsgResult(true, "Error code: " + toErrCode);
    }
    if (StringUtils.isEmpty(toErrMsg)) {
      return new ConvertErrMsgResult(false, "");
    }
    if (!Boolean.TRUE.equals(asDefault) && (StringUtils.isEmpty(matchedText) || !msg.contains(matchedText))) {
      return new ConvertErrMsgResult(false, "");
    }

    return new ConvertErrMsgResult(true, toErrMsg);
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public static class ConvertErrMsgResult {
    private boolean matched;
    private String msg;
  }

}
