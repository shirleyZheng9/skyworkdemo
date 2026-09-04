package com.iwhalecloud.bote.config.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 单个线程池的配置
 *
 * @author bianjp
 * @since 2024-11-06
 */
@Getter
@Setter
@ToString
public class ThreadPoolProperties {
  /** 线程名称前缀。默认使用线程池名称 */
  private String threadNamePrefix;
  /** 并发数限制 */
  private int concurrencyLimit = 1000;
  /** 等待执行结束的最大时间 */
  private Duration awaitTerminationPeriod = Duration.ofSeconds(5);
  /** 是否继承上下文（HTTP 请求、登录信息等线程本地变量） */
  private boolean inheritContext = true;
}
