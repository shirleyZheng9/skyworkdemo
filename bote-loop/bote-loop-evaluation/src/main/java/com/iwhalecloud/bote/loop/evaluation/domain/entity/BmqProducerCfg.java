package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * BMQ生产者配置
 */
public class BmqProducerCfg {

  private String topic;
  private String cluster;

  public BmqProducerCfg() {
  }

  public BmqProducerCfg(String topic, String cluster) {
    this.topic = topic;
    this.cluster = cluster;
  }

  // Getters and Setters
  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getCluster() {
    return cluster;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }
}
