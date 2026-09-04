package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 实验项目评估配置
 */
public class ExptItemEvalConf {

  private Integer concurNum;
  private Integer intervalSecond;
  private Integer zombieSecond;

  // 默认值常量
  private static final int DEFAULT_ITEM_EVAL_CONCUR_NUM = 3;
  private static final int DEFAULT_ITEM_EVAL_INTERVAL_SECOND = 20;
  private static final int DEFAULT_ITEM_ZOMBIE_SECOND = 60 * 20;

  public Integer getConcurNum() {
    return concurNum != null && concurNum > 0 ? concurNum : DEFAULT_ITEM_EVAL_CONCUR_NUM;
  }

  public Integer getInterval() {
    return intervalSecond != null && intervalSecond > 0 ? intervalSecond : DEFAULT_ITEM_EVAL_INTERVAL_SECOND;
  }

  public Integer getZombieSecond() {
    return zombieSecond != null && zombieSecond > 0 ? zombieSecond : DEFAULT_ITEM_ZOMBIE_SECOND;
  }

  // Getters and Setters
  public void setConcurNum(Integer concurNum) {
    this.concurNum = concurNum;
  }

  public void setIntervalSecond(Integer intervalSecond) {
    this.intervalSecond = intervalSecond;
  }

  public void setZombieSecond(Integer zombieSecond) {
    this.zombieSecond = zombieSecond;
  }
}
