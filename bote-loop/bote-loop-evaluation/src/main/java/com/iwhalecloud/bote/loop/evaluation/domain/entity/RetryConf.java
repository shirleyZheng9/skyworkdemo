package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 重试配置
 */
@Getter
@Setter
public class RetryConf {

  private Integer retryTimes = 0;
  private Integer retryIntervalSecond = 20;
  private Boolean isInDebt;
}
