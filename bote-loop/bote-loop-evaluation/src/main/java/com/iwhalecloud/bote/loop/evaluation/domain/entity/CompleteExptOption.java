package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 完成实验选项
 * 对应Go: CompleteExptOption
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteExptOption {

  private ExptStatus status;
  private String statusMessage;
  private String cid;

  public static CompleteExptOptionFn withStatus(ExptStatus param) {
    return option -> {
      option.setStatus(param);
    };
  }

  public static CompleteExptOptionFn withStatusMessage(String msg) {
    return option -> {
      String param = msg;
      if (msg != null && msg.length() > 200) {
        param = msg.substring(0, 200);
      }
      option.setStatusMessage(param);
    };
  }

}
