package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.Map;

/**
 * 错误重试控制配置
 */
public class ErrRetryCtrl {

  private RetryConf retryConf;
  private Map<String, RetryConf> errRetryConf;

  public RetryConf getRetryConf(Exception err) {
    if (err == null) {
      return null;
    }

    String errMsg = err.getMessage();
    if (errRetryConf != null && errMsg != null) {
      for (Map.Entry<String, RetryConf> entry : errRetryConf.entrySet()) {
        if (errMsg.contains(entry.getKey())) {
          return entry.getValue();
        }
      }
    }

    return retryConf;
  }

  // Getters and Setters
  public RetryConf getRetryConf() {
    return retryConf;
  }

  public void setRetryConf(RetryConf retryConf) {
    this.retryConf = retryConf;
  }

  public Map<String, RetryConf> getErrRetryConf() {
    return errRetryConf;
  }

  public void setErrRetryConf(Map<String, RetryConf> errRetryConf) {
    this.errRetryConf = errRetryConf;
  }
}
