package com.iwhalecloud.bote.loop.prompt.domain.component.conf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "prompt-hub-rate-limit-config")
public class PromptHubRateLimitConfig {

  // Getter和Setter方法
  /**
   * 默认最大QPS
   * 迁移对应关系: Go语言promptHubRateLimitConfig.DefaultMaxQPS (int)
   * - 功能: 默认的最大QPS限制
   * - 类型: Go的int对应Java的Integer
   * - 用途: 当没有特定空间配置时使用
   * - 默认值: 500
   */

  private Integer defaultMaxQps = 500;

  /**
   * 空间特定最大QPS
   * 迁移对应关系: Go语言promptHubRateLimitConfig.SpaceMaxQPS (map[int64]int)
   * - 功能: 每个空间的最大QPS限制
   * - 类型: Go的map[int64]int对应Java的Map<Long, Integer>
   * - 用途: 为特定空间设置不同的QPS限制
   */
  private Map<Long, Integer> spaceMaxQps = new ConcurrentHashMap<>();

  public void setSpaceMaxQps(Map<Long, Integer> spaceMaxQps) {
    this.spaceMaxQps = spaceMaxQps != null ? new ConcurrentHashMap<>(spaceMaxQps) : new ConcurrentHashMap<>();
  }

  /**
   * 获取指定空间的最大QPS
   *
   * @param spaceId 空间ID
   * @return 最大QPS，如果没有配置则返回默认值
   */
  public Integer getMaxQPSForSpace(Long spaceId) {
    if (spaceMaxQps != null && spaceMaxQps.containsKey(spaceId)) {
      return spaceMaxQps.get(spaceId);
    }
    return defaultMaxQps;
  }
}
