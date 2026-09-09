package com.iwhalecloud.bote.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.dashboard.ConversationMessageTrendCacheDTO;
import com.iwhalecloud.bote.dto.dashboard.ConversationMessageTrendCountDTO;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricCountDTO;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricsCacheDTO;
import com.iwhalecloud.bote.dto.dashboard.TodayDynamicsCacheDTO;
import com.iwhalecloud.bote.dto.dashboard.TodayDynamicsVO;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 数据概览看板的缓存访问组件。
 *
 * <p>设计约束：</p>
 * <ol>
 *   <li>数据库始终是统计结果的真实数据源，Redis 只缓存最终的文件大类数量，不维护增量计数；</li>
 *   <li>缓存键包含 tenantId，确保不同租户之间的数据完全隔离；</li>
 *   <li>缓存使用逻辑过期：正常结果 5～6 分钟后异步刷新，旧值继续保留 24 小时；</li>
 *   <li>全零结果 1 分钟后异步刷新，防止缓存穿透同时缩短新租户数据的可见延迟；</li>
 *   <li>本类只负责缓存值读写，刷新任务的线程池、限流和锁由通用协调器统一处理。</li>
 * </ol>
 *
 * <p>项目关闭了 Spring 标准 Cache 自动配置，因此这里沿用项目统一的
 * {@link CacheFactory}/{@link ICacheClient}，不使用 {@code @Cacheable} 或 RedisTemplate。</p>
 *
 * @author zhengxueli
 * @since 2026-08-28
 */
@Component
public class DataDashboardCache {

  /**
   * 单次缓存读取的不可变结果。
   *
   * <p>它定义在 {@code DataDashboardCache} 内部，是因为目前只有数据看板缓存需要表达
   * “缓存值仍可返回，但逻辑有效期已经到达”这一状态，没有必要为两个字段建立独立的
   * 公共 Java 文件。使用泛型保留同一个看板缓存组件后续复用不同数据类型的能力。</p>
   *
   * <p>{@code record} 会由编译器生成构造方法、{@link #value()}、
   * {@link #refreshRequired()}、equals、hashCode 和 toString；此处不需要手写样板代码。</p>
   *
   * @param value 从 Redis 读取并完成清洗后的业务数据
   * @param refreshRequired {@code true} 表示数据可立即返回，但应在返回后提交后台刷新
   * @param <T> 当前缓存保存的业务数据类型
   */
  public record CacheSnapshot<T>(T value, boolean refreshRequired) {
  }

  /**
   * CacheFactory 会负责拼接缓存分组和此前缀，业务方法只需再提供 tenantId 与版本号。
   * v2 用于文件类型新增“音视频”后平滑切换缓存，不必扫描并删除旧键。
   */
  private static final String KNOWLEDGE_FILE_DISTRIBUTION_PREFIX =
    "dashboard:knowledge-file-distribution:";

  /** 顶部资源概览缓存前缀；key 会继续拼接 tenantId、统计日期和版本。 */
  private static final String OVERVIEW_METRICS_PREFIX = "dashboard:overview-metrics:";

  /** 每日对话消息量趋势缓存前缀；key 会继续拼接 tenantId、结束日期和版本。 */
  private static final String CONVERSATION_MESSAGE_TREND_PREFIX =
    "dashboard:conversation-message-trend:";

  /** 今日动态缓存前缀；key 会继续拼接 tenantId、统计日期和版本。 */
  private static final String TODAY_DYNAMICS_PREFIX = "dashboard:today-dynamics:";

  private static final String CACHE_VERSION = "v2";

  /** 顶部概览统计口径升级为净变化后使用新 key，避免读到旧口径缓存。 */
  private static final String OVERVIEW_METRICS_CACHE_VERSION = "v3";

  /** 顶部概览 key 中的日期格式，使用自然日隔离“较昨日净变化”。 */
  private static final DateTimeFormatter OVERVIEW_DATE_FORMATTER =
    DateTimeFormatter.BASIC_ISO_DATE;

  /** 保存在原 Map 中的内部字段，兼容已上线的 v1 数量缓存。 */
  private static final String REFRESH_AFTER_EPOCH_MILLIS = "_refreshAfterEpochMillis";

  /** 正常数据至少保持 5 分钟新鲜。 */
  private static final int NORMAL_FRESH_SECONDS = 5 * 60;

  /** 在正常 TTL 上增加 0～60 秒随机抖动，降低缓存雪崩风险。 */
  private static final int NORMAL_FRESH_JITTER_BOUND_SECONDS = 61;

  /** 没有任何有效文档时短暂缓存，防止同一空租户反复扫描数据库。 */
  private static final int EMPTY_FRESH_SECONDS = 60;

  /** 物理缓存远长于逻辑有效期，后台刷新失败时仍可返回旧值。 */
  private static final int PHYSICAL_TTL_SECONDS = 24 * 60 * 60;

  /** 顶部概览正常缓存 30 秒，首页高频访问时可以明显削峰。 */
  private static final int OVERVIEW_FRESH_SECONDS = 30;

  /** 顶部概览逻辑过期增加 0～30 秒抖动，避免多租户同时刷新。 */
  private static final int OVERVIEW_FRESH_JITTER_BOUND_SECONDS = 31;

  /** 顶部概览物理保留 2 天；key 带日期，跨日后自然读取新 key。 */
  private static final int OVERVIEW_PHYSICAL_TTL_SECONDS = 2 * 24 * 60 * 60;

  /** 消息趋势正常缓存 30 秒，用于首页高频访问削峰。 */
  private static final int CONVERSATION_TREND_FRESH_SECONDS = 30;

  /** 消息趋势逻辑过期增加 0～30 秒抖动，避免多租户同时刷新。 */
  private static final int CONVERSATION_TREND_FRESH_JITTER_BOUND_SECONDS = 31;

  /** 消息趋势物理保留 2 天；key 带结束日期，跨日后自然读取新 key。 */
  private static final int CONVERSATION_TREND_PHYSICAL_TTL_SECONDS = 2 * 24 * 60 * 60;

  /** 今日动态正常缓存 30 秒，兼顾首页性能和数据新鲜度。 */
  private static final int TODAY_DYNAMICS_FRESH_SECONDS = 30;

  /** 今日动态逻辑过期增加 0～30 秒抖动，避免多租户同时刷新。 */
  private static final int TODAY_DYNAMICS_FRESH_JITTER_BOUND_SECONDS = 31;

  /** 今日动态物理保留 2 天；key 带日期，跨日后自然读取新 key。 */
  private static final int TODAY_DYNAMICS_PHYSICAL_TTL_SECONDS = 2 * 24 * 60 * 60;

  private final ICacheClient cacheClient;

  private final ICacheClient overviewMetricsCacheClient;

  private final ICacheClient conversationMessageTrendCacheClient;

  private final ICacheClient todayDynamicsCacheClient;

  public DataDashboardCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(
      CacheConsts.GROUP_PLATFORM,
      KNOWLEDGE_FILE_DISTRIBUTION_PREFIX
    );
    this.overviewMetricsCacheClient = cacheFactory.getCacheClient(
      CacheConsts.GROUP_PLATFORM,
      OVERVIEW_METRICS_PREFIX
    );
    this.conversationMessageTrendCacheClient = cacheFactory.getCacheClient(
      CacheConsts.GROUP_PLATFORM,
      CONVERSATION_MESSAGE_TREND_PREFIX
    );
    this.todayDynamicsCacheClient = cacheFactory.getCacheClient(
      CacheConsts.GROUP_PLATFORM,
      TODAY_DYNAMICS_PREFIX
    );
  }

  /**
   * 查询顶部资源概览缓存。
   *
   * <p>缓存 key 包含统计日期，因此 0 点后会自动切换到新 key，不会把昨天的“较昨日净变化”
   * 展示到今天。命中逻辑过期缓存时，调用方仍可立即返回旧值并触发后台刷新。</p>
   */
  @Nullable
  public CacheSnapshot<List<OverviewMetricCountDTO>> getOverviewMetrics(
    Long tenantId,
    LocalDate statisticsDate
  ) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(statisticsDate, "统计日期不能为空");

    // 只访问一次 Redis，缓存值中同时包含业务数据和逻辑过期时间。
    String json = overviewMetricsCacheClient.opsForValue().get(
      buildOverviewMetricsKey(tenantId, statisticsDate)
    );
    if (StringUtils.isBlank(json)) {
      return null;
    }

    OverviewMetricsCacheDTO cachedValue =
      JsonUtil.parseJsonRequired(json, new TypeReference<OverviewMetricsCacheDTO>() {
      });
    Long refreshAfter = cachedValue.getRefreshAfterEpochMillis();
    List<OverviewMetricCountDTO> metrics = cachedValue.getMetrics() == null
      ? List.of()
      : List.copyOf(cachedValue.getMetrics());

    return new CacheSnapshot<>(
      metrics,
      refreshAfter == null || refreshAfter <= System.currentTimeMillis()
    );
  }

  /**
   * 写入顶部资源概览缓存。
   *
   * <p>这里只缓存聚合后的少量指标行，不缓存 SQL 明细。逻辑 TTL 较短，用于首页高频访问削峰；
   * 物理 TTL 较长，用于后台刷新失败时继续返回上一版数据。</p>
   */
  public void putOverviewMetrics(
    Long tenantId,
    LocalDate statisticsDate,
    List<OverviewMetricCountDTO> metrics
  ) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(statisticsDate, "统计日期不能为空");

    OverviewMetricsCacheDTO cachedValue = new OverviewMetricsCacheDTO();
    cachedValue.setMetrics(metrics == null ? List.of() : new ArrayList<>(metrics));
    cachedValue.setRefreshAfterEpochMillis(
      System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
        OVERVIEW_FRESH_SECONDS + ThreadLocalRandom.current().nextInt(OVERVIEW_FRESH_JITTER_BOUND_SECONDS)
      )
    );

    overviewMetricsCacheClient.opsForValue().set(
      buildOverviewMetricsKey(tenantId, statisticsDate),
      JsonUtil.toJsonString(cachedValue),
      OVERVIEW_PHYSICAL_TTL_SECONDS + ThreadLocalRandom.current().nextInt(60 * 60),
      TimeUnit.SECONDS
    );
  }

  /**
   * 查询每日对话消息量趋势缓存。
   *
   * <p>缓存 key 包含结束日期。首页在 0 点后会访问新的 key，不会把昨天的 30 天窗口
   * 继续作为今天的趋势返回。命中逻辑过期缓存时，调用方仍可立即返回旧值并触发后台刷新。</p>
   */
  @Nullable
  public CacheSnapshot<List<ConversationMessageTrendCountDTO>> getConversationMessageTrend(
    Long tenantId,
    LocalDate endDate
  ) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(endDate, "统计结束日期不能为空");

    // 只访问一次 Redis，缓存值中同时包含聚合结果和逻辑过期时间。
    String json = conversationMessageTrendCacheClient.opsForValue().get(
      buildConversationMessageTrendKey(tenantId, endDate)
    );
    if (StringUtils.isBlank(json)) {
      return null;
    }

    // 反序列化为专用缓存 DTO，避免直接反序列化 final 字段 VO 带来的兼容风险。
    ConversationMessageTrendCacheDTO cachedValue =
      JsonUtil.parseJsonRequired(json, new TypeReference<ConversationMessageTrendCacheDTO>() {
      });
    // 缓存中没有 counts 时按空列表处理，Service 会补齐 30 天全零结果。
    List<ConversationMessageTrendCountDTO> counts = cachedValue.getCounts() == null
      ? List.of()
      : List.copyOf(cachedValue.getCounts());
    // refreshAfter 为空表示旧格式或异常缓存，需要后台刷新一次。
    Long refreshAfter = cachedValue.getRefreshAfterEpochMillis();

    return new CacheSnapshot<>(
      counts,
      refreshAfter == null || refreshAfter <= System.currentTimeMillis()
    );
  }

  /**
   * 写入每日对话消息量趋势缓存。
   *
   * <p>只缓存数据库按天聚合后的少量行。逻辑 TTL 较短，保证首页重复打开时稳定快速；
   * 物理 TTL 较长，后台刷新失败时仍可返回上一版趋势。</p>
   */
  public void putConversationMessageTrend(
    Long tenantId,
    LocalDate endDate,
    List<ConversationMessageTrendCountDTO> counts
  ) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(endDate, "统计结束日期不能为空");

    // 组装缓存 DTO，保留逻辑过期时间和数据库聚合结果。
    ConversationMessageTrendCacheDTO cachedValue = new ConversationMessageTrendCacheDTO();
    // counts 做防御性复制，避免调用方后续修改影响缓存序列化内容。
    cachedValue.setCounts(counts == null ? List.of() : new ArrayList<>(counts));
    // 加入随机抖动，降低多租户同时刷新造成的数据库毛刺。
    cachedValue.setRefreshAfterEpochMillis(
      System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
        CONVERSATION_TREND_FRESH_SECONDS
          + ThreadLocalRandom.current().nextInt(CONVERSATION_TREND_FRESH_JITTER_BOUND_SECONDS)
      )
    );

    // key 带结束日期，物理 TTL 保留 2 天用于后台刷新失败兜底。
    conversationMessageTrendCacheClient.opsForValue().set(
      buildConversationMessageTrendKey(tenantId, endDate),
      JsonUtil.toJsonString(cachedValue),
      CONVERSATION_TREND_PHYSICAL_TTL_SECONDS + ThreadLocalRandom.current().nextInt(60 * 60),
      TimeUnit.SECONDS
    );
  }

  /**
   * 查询今日动态缓存。
   *
   * <p>缓存 key 包含统计日期。0 点后自动访问新 key，避免昨天的“今日”指标继续展示。
   * 逻辑过期时调用方仍可返回旧值，并提交后台刷新。</p>
   */
  @Nullable
  public CacheSnapshot<TodayDynamicsVO> getTodayDynamics(Long tenantId, LocalDate statisticsDate) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(statisticsDate, "统计日期不能为空");

    // 只读取一次 Redis，业务数据和逻辑过期时间都放在同一个 JSON 值中。
    String json = todayDynamicsCacheClient.opsForValue().get(
      buildTodayDynamicsKey(tenantId, statisticsDate)
    );
    if (StringUtils.isBlank(json)) {
      return null;
    }

    // 反序列化为可变缓存 DTO，避免直接反序列化 final 字段 VO 的兼容风险。
    TodayDynamicsCacheDTO cachedValue =
      JsonUtil.parseJsonRequired(json, new TypeReference<TodayDynamicsCacheDTO>() {
      });
    // refreshAfter 为空表示旧格式或异常缓存，需要后台刷新一次。
    Long refreshAfter = cachedValue.getRefreshAfterEpochMillis();
    // 缓存 DTO 转成接口 VO，保证 Controller 返回结构只有业务字段。
    TodayDynamicsVO value = new TodayDynamicsVO(
      cachedValue.getNewAgentCount(),
      cachedValue.getNewKnowledgeCount(),
      cachedValue.getModelInvokeCount(),
      cachedValue.getBuiltDocumentCount(),
      cachedValue.getStatTime()
    );

    return new CacheSnapshot<>(
      value,
      refreshAfter == null || refreshAfter <= System.currentTimeMillis()
    );
  }

  /**
   * 写入今日动态缓存。
   *
   * <p>今日动态只缓存最终聚合值。逻辑 TTL 短，保证首页重复打开时快速；物理 TTL 长，
   * 后台刷新失败时可以继续返回上一版数据。</p>
   */
  public void putTodayDynamics(Long tenantId, LocalDate statisticsDate, TodayDynamicsVO value) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(statisticsDate, "统计日期不能为空");
    Assert.notNull(value, "今日动态统计结果不能为空");

    // 组装缓存 DTO，保存接口字段和逻辑过期时间。
    TodayDynamicsCacheDTO cachedValue = new TodayDynamicsCacheDTO();
    // 今日新增智能体数量来自概览真实统计。
    cachedValue.setNewAgentCount(value.getNewAgentCount());
    // 今日新增知识库数量来自概览真实统计。
    cachedValue.setNewKnowledgeCount(value.getNewKnowledgeCount());
    // 今日大模型调用次数来自模型调用日志当前表。
    cachedValue.setModelInvokeCount(value.getModelInvokeCount());
    // 今日知识构建文档来自当天新增且已构建完成的知识库文档。
    cachedValue.setBuiltDocumentCount(value.getBuiltDocumentCount());
    // 统计时间用于前端或排查人员判断数据新鲜度。
    cachedValue.setStatTime(value.getStatTime());
    // 加入随机抖动，降低多租户同时刷新造成的数据库毛刺。
    cachedValue.setRefreshAfterEpochMillis(
      System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
        TODAY_DYNAMICS_FRESH_SECONDS
          + ThreadLocalRandom.current().nextInt(TODAY_DYNAMICS_FRESH_JITTER_BOUND_SECONDS)
      )
    );

    // key 带统计日期，物理 TTL 保留 2 天用于后台刷新失败兜底。
    todayDynamicsCacheClient.opsForValue().set(
      buildTodayDynamicsKey(tenantId, statisticsDate),
      JsonUtil.toJsonString(cachedValue),
      TODAY_DYNAMICS_PHYSICAL_TTL_SECONDS + ThreadLocalRandom.current().nextInt(60 * 60),
      TimeUnit.SECONDS
    );
  }

  /**
   * 查询当前租户的知识库文件大类数量缓存。
   *
   * <p>返回快照而不是只返回 Map，是因为缓存值可以继续使用，但逻辑有效期到达后还需要
   * 通知 Service 提交后台刷新。Redis 中只有一个 JSON 值，因此不会因为判断逻辑过期
   * 额外增加一次 Redis 网络请求。</p>
   *
   * @param tenantId 当前登录租户 ID
   * @return 缓存不存在时返回 null；命中时返回数量 Map 及是否需要后台刷新
   */
  @Nullable
  public CacheSnapshot<Map<String, Long>> getKnowledgeFileDistribution(Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    String json = cacheClient.opsForValue().get(buildKnowledgeFileDistributionKey(tenantId));
    if (StringUtils.isBlank(json)) {
      return null;
    }
    // 从 Redis 中读取缓存，并把缓存里的“刷新时间”取出来
    Map<String, Long> cachedValue = JsonUtil.parseJsonRequired(json, new TypeReference<Map<String, Long>>() {
    });
    // 内部时间字段不属于业务分类，必须先移除，避免被 Service 当成第五种文件类型。
    Long refreshAfter = cachedValue.remove(REFRESH_AFTER_EPOCH_MILLIS);

    // 兼容已经写入 Redis 的旧版 v1 Map：旧值没有逻辑刷新时间，数量仍可立即返回，
    // 同时将 refreshRequired 标为 true，由后台查询并覆盖成新格式，不需要清空 Redis。
    boolean refreshRequired = refreshAfter == null || refreshAfter <= System.currentTimeMillis();
    return new CacheSnapshot<>(
      // 返回防御性只读副本，避免调用方修改反序列化得到的缓存内容。
      Collections.unmodifiableMap(new HashMap<>(cachedValue)),
      refreshRequired
    );
  }

  /**
   * 缓存当前租户的知识库文件大类数量。
   *
   * <p>逻辑有效期和 Redis 物理 TTL 分离：</p>
   * <ul>
   *   <li>逻辑有效期到达后，接口继续返回旧数据并在后台刷新；</li>
   *   <li>物理 TTL 约 24 小时，仅用于清理长期无人访问且没有变更事件的租户缓存；</li>
   *   <li>刷新失败不会主动删除旧值，因此数据库故障不会把压力传递给查询接口。</li>
   * </ul>
   *
   * @param tenantId 当前登录租户 ID
   * @param countByCategory 固定文件大类数量
   * @param noData 文件大类数量是否全部为 0
   */
  public void putKnowledgeFileDistribution(Long tenantId,
                                           Map<String, Long> countByCategory,
                                           boolean noData) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(countByCategory, "文件类型统计结果不能为空");

    // 空数据更快刷新，减少新租户上传首个文档后的展示延迟；正常结果加入随机抖动，
    // 避免大量租户在同一秒进入后台刷新队列。
    int freshSeconds = noData
      ? EMPTY_FRESH_SECONDS
      : NORMAL_FRESH_SECONDS + ThreadLocalRandom.current().nextInt(NORMAL_FRESH_JITTER_BOUND_SECONDS);
    Map<String, Long> cachedValue = new HashMap<>(countByCategory);
    cachedValue.put(
      REFRESH_AFTER_EPOCH_MILLIS,
      System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(freshSeconds)
    );

    // 时间字段与文件大类数量写在同一个 JSON 中，查询时只需访问 Redis 一次。
    cacheClient.opsForValue().set(
      buildKnowledgeFileDistributionKey(tenantId),
      JsonUtil.toJsonString(cachedValue),
      PHYSICAL_TTL_SECONDS + ThreadLocalRandom.current().nextInt(60 * 60),
      TimeUnit.SECONDS
    );
  }

  /** 构造租户隔离且带口径版本的业务键。 */
  private String buildKnowledgeFileDistributionKey(Long tenantId) {
    return tenantId + ":" + CACHE_VERSION;
  }

  /** 构造按租户、日期、口径版本隔离的顶部概览缓存键。 */
  private String buildOverviewMetricsKey(Long tenantId, LocalDate statisticsDate) {
    return tenantId + ":" + statisticsDate.format(OVERVIEW_DATE_FORMATTER)
      + ":" + OVERVIEW_METRICS_CACHE_VERSION;
  }

  /** 构造按租户、结束日期、口径版本隔离的每日消息趋势缓存键。 */
  private String buildConversationMessageTrendKey(Long tenantId, LocalDate endDate) {
    return tenantId + ":" + endDate.format(OVERVIEW_DATE_FORMATTER) + ":" + CACHE_VERSION;
  }

  /** 构造按租户、日期、口径版本隔离的今日动态缓存键。 */
  private String buildTodayDynamicsKey(Long tenantId, LocalDate statisticsDate) {
    return tenantId + ":" + statisticsDate.format(OVERVIEW_DATE_FORMATTER) + ":" + CACHE_VERSION;
  }

}
