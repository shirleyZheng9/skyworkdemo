package com.iwhalecloud.bote.dto.dashboard;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 顶部资源概览数据库统计结果。
 *
 * <p>该 DTO 只承接 Mapper 返回的聚合行，不直接暴露给前端。Service 会根据固定展示顺序
 * 补齐缺失项、清洗 null/负数，并转换为 {@link OverviewMetricVO}。</p>
 *
 * @author zhengxueli
 * @since 2026-08-31
 */
@Getter
@Setter
@ToString
public class OverviewMetricCountDTO {

  /**
   * 页面卡片编码，例如 agent、knowledge、model、tool。
   * <p>该值来自 SQL 中写死的 metric_code，不直接使用表名，避免暴露数据库结构给前端。</p>
   */
  private String code;

  /**
   * 当前口径下的有效资源总量。
   * <p>每个 SQL 分支独立 COUNT 自己的资源主表；插件/MCP会先分别统计，再合并成 tool。</p>
   */
  private Long total;

  /**
   * 今日新增数量。
   * <p>与 total 使用完全相同的过滤口径，只额外增加 created_time 的左闭右开时间边界。</p>
   */
  private Long todayIncrease;
}
