package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 实验执行配置
 */
@Getter
@Setter
public class ExptExecConf {

  /* 实验消息推送时间(秒)*/
  private Integer daemonIntervalSecond = 10;
  /* 实验超时时间(秒)*/
  private Integer zombieIntervalSecond = 3600;
  private Integer spaceExptConcurLimit = 3600;
  private ExptItemEvalConf exptItemEvalConf = new ExptItemEvalConf();
}
