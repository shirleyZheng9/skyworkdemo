package com.iwhalecloud.bote.dto.dashboard;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 顶部资源概览 Redis 缓存值。
 *
 * <p>该对象只在后端缓存层使用，不对前端暴露。之所以不用简单 Map，是因为概览指标
 * 同时包含总量和较昨日净变化两个数字，使用结构化对象能避免缓存字段含义混乱。</p>
 *
 * @author zhengxueli
 * @since 2026-08-31
 */
@Getter
@Setter
@ToString
public class OverviewMetricsCacheDTO {

  /**
   * 逻辑过期时间。
   * <p>Redis 物理 TTL 会保留更久；到达该时间后，接口仍可先返回旧值，同时后台刷新。</p>
   */
  private Long refreshAfterEpochMillis;

  /**
   * 数据库聚合结果。
   * <p>保存 Mapper 原始聚合行，由 Service 统一补齐固定顺序、清洗 null 和负数。</p>
   */
  private List<OverviewMetricCountDTO> metrics;
}
