package com.iwhalecloud.bote.dto.dashboard;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 每日对话消息量趋势 Redis 缓存值。
 *
 * <p>缓存 Mapper 聚合后的少量日期行，而不是缓存消息明细或最终 VO，既减少 Redis 体积，
 * 也让 Service 继续统一负责补齐 30 天和异常值清洗。</p>
 *
 * @author zhengxueli
 * @since 2026-09-01
 */
@Getter
@Setter
@ToString
public class ConversationMessageTrendCacheDTO {

  /**
   * 逻辑过期时间。
   * <p>到达该时间后接口仍先返回旧趋势，同时后台刷新，保证首页加载不被大表聚合拖慢。</p>
   */
  private Long refreshAfterEpochMillis;

  /**
   * 最近三十天内有消息日期的聚合结果。
   * <p>缺失日期不在缓存中保存，由 Service 每次按当前口径补 0。</p>
   */
  private List<ConversationMessageTrendCountDTO> counts;
}
