package com.iwhalecloud.bote.loop.data.domain.component.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("snapshot.retry")
public class SnapshotRetryProperties {

  private Long maxRetryTimes;
  private Long retryIntervalMs;
  private Long maxProcessingTimeS;

}
