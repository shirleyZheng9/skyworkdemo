package com.iwhalecloud.bote.loop.data.domain.component.conf;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("job.mq.producer")
public class ProducerConfigProperties {

  private String topic;
  private String tag;
  private List<String> addr;
  private Long produceTimeout;
  private String producerGroup;

}
