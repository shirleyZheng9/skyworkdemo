package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 实验消费者配置
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "expt.consumer.conf")
public class ExptConsumerConf {

  private Integer exptExecWorkerNum = 50;
  private Integer exptItemEvalWorkerNum = 200;
  private ExptExecConf exptExecConf = new ExptExecConf();
  private Map<Long, ExptExecConf> spaceExptExecConf = Maps.newHashMap();

  public ExptExecConf getExptExecConfBySpaceId(Long spaceId) {
    return spaceExptExecConf.containsKey(spaceId) ? spaceExptExecConf.get(spaceId) : exptExecConf;
  }

}
