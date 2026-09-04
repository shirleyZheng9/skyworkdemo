package com.iwhalecloud.bote.loop.data.domain.component.conf;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("consumer.configs")
public class ConsumerConfigProperties {

  private List<String> addr;
  private String topic;
  private String consumerGroup;
  private boolean orderly;
  private String tagExpression;
  private Integer consumeGoroutineNums;
  private Long consumeTimeout;

}
