package com.iwhalecloud.bote.service.dashboard.impl;

import com.iwhalecloud.bote.cache.DataDashboardCache;
import com.iwhalecloud.bote.cache.DataDashboardCache.CacheSnapshot;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.dto.dashboard.AgentModeCountDTO;
import com.iwhalecloud.bote.dto.dashboard.ConversationMessageTrendCountDTO;
import com.iwhalecloud.bote.dto.dashboard.ConversationMessageTrendItemVO;
import com.iwhalecloud.bote.dto.dashboard.DistributionItemVO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageDistributionItemVO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageStatDTO;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricCountDTO;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricVO;
import com.iwhalecloud.bote.dto.dashboard.TodayDynamicsVO;
import com.iwhalecloud.bote.mapper.dashboard.DataDashboardMapper;
import com.iwhalecloud.bote.service.dashboard.IDataDashboardService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 数据概览看板服务实现。
 *
 * @author zhengxueli
 * @since 2026-08-28
 */
@Service
public class DataDashboardServiceImpl implements IDataDashboardService {

  private static final Logger logger = LoggerFactory.getLogger(DataDashboardServiceImpl.class);


  private static final Map<String, String> AGENT_MODE_NAMES = createAgentModeNames();

  /**
   * 顶部资源卡片固定展示顺序。
   * <p>使用 LinkedHashMap 是为了让后端返回顺序稳定：智能体、知识库、模型、插件/MCP。
   * 前端即使直接按数组渲染，也不会因为 SQL 或缓存返回顺序变化导致卡片跳动。</p>
   */
  private static final Map<String, String> OVERVIEW_METRIC_NAMES = createOverviewMetricNames();

  /** 固定返回顺序：文档、表格、图片、音视频、其他。 */
  private static final Map<String, String> KNOWLEDGE_FILE_CATEGORY_NAMES =
    createKnowledgeFileCategoryNames();

  private static final String CATEGORY_OTHER = "other";

  private static final int CONVERSATION_TREND_DAYS = 30;

  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

  private static final BigDecimal ONE_HUNDRED_WITH_SCALE = new BigDecimal("100.00");

  private static final BigDecimal ONE_HUNDREDTH = new BigDecimal("0.01");

  /**
   * 计算最大余数法时保留的中间精度。最终接口仍只返回两位小数；较高的中间精度用于
   * 正确判断哪一个分类的小数余数最大。
   */
  private static final int RATIO_INTERMEDIATE_SCALE = 10;

  private final DataDashboardMapper dataDashboardMapper;

  private final DataDashboardCache dataDashboardCache;

  private final KnowledgeFileDistributionRefreshService knowledgeFileDistributionRefreshService;

  private final OverviewMetricsRefreshService overviewMetricsRefreshService;

  private final ConversationMessageTrendRefreshService conversationMessageTrendRefreshService;

  private final TodayDynamicsRefreshService todayDynamicsRefreshService;

  public DataDashboardServiceImpl(
    DataDashboardMapper dataDashboardMapper,
    DataDashboardCache dataDashboardCache,
    KnowledgeFileDistributionRefreshService knowledgeFileDistributionRefreshService,
    OverviewMetricsRefreshService overviewMetricsRefreshService,
    ConversationMessageTrendRefreshService conversationMessageTrendRefreshService,
    TodayDynamicsRefreshService todayDynamicsRefreshService
  ) {
    this.dataDashboardMapper = dataDashboardMapper;
    this.dataDashboardCache = dataDashboardCache;
    this.knowledgeFileDistributionRefreshService = knowledgeFileDistributionRefreshService;
    this.overviewMetricsRefreshService = overviewMetricsRefreshService;
    this.conversationMessageTrendRefreshService = conversationMessageTrendRefreshService;
    this.todayDynamicsRefreshService = todayDynamicsRefreshService;
  }

  @Override
  public List<OverviewMetricVO> queryOverviewMetrics(Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");

    /*
     * 缓存 key 按自然日隔离。这样 0 点后会读取新的 yyyyMMdd key，不会把昨天的
     * “今日新增”继续展示到今天。真实数据库查询的 todayStart/tomorrowStart 也由同一个statisticsDate 推导，保证 key 和统计口径一致。
     */
    LocalDate statisticsDate = LocalDate.now();

    CacheSnapshot<List<OverviewMetricCountDTO>> cachedSnapshot =
      getCachedOverviewMetrics(tenantId, statisticsDate);
    if (cachedSnapshot != null) {
      if (cachedSnapshot.refreshRequired()) {
        overviewMetricsRefreshService.requestRefresh(tenantId, statisticsDate);
      }
      return buildOverviewMetrics(cachedSnapshot.value());
    }

    /*
     * 冷缓存或 Redis 异常时同步查库，保证首次访问返回真实统计。
     * 查询结果会由刷新服务写入 Redis；写缓存失败不影响本次接口返回。
     */
    List<OverviewMetricCountDTO> databaseCounts =
      overviewMetricsRefreshService.loadCurrentMetrics(tenantId, statisticsDate);
    return buildOverviewMetrics(databaseCounts);
  }

  /**
   * 尝试读取顶部概览缓存。
   *
   * <p>Redis 是性能兜底，不是唯一数据源。读取异常时返回 null，调用方会同步查库，
   * 避免因为缓存组件故障导致首页顶部卡片不可用。</p>
   */
  private CacheSnapshot<List<OverviewMetricCountDTO>> getCachedOverviewMetrics(
    Long tenantId,
    LocalDate statisticsDate
  ) {
    try {
      return dataDashboardCache.getOverviewMetrics(tenantId, statisticsDate);
    }
    catch (RuntimeException e) {
      logger.warn("读取顶部资源概览缓存失败，转为数据库查询，tenantId={}, statisticsDate={}",
        tenantId, statisticsDate, e);
      return null;
    }
  }

  /**
   * 组装顶部资源概览接口结果。
   *
   * <p>缓存和数据库都可能只返回部分指标，因此这里统一按固定顺序补齐四张卡片。
   * 所有数量都经过归零保护，避免 null 或异常负数透出给前端。</p>
   */
  private List<OverviewMetricVO> buildOverviewMetrics(List<OverviewMetricCountDTO> databaseCounts) {
    /*
     * Mapper 正常会返回四类指标；Service 仍按固定卡片顺序补零和忽略未知编码，
     * 防止 SQL 调整、空库或异常数据导致前端卡片顺序抖动。
     */
    // 先按编码建 Map，后面再按固定顺序输出；未知编码直接忽略，不影响页面已有卡片。
    Map<String, OverviewMetricCountDTO> countByCode = new HashMap<>();
    if (databaseCounts != null) {
      for (OverviewMetricCountDTO item : databaseCounts) {
        // SQL 正常不会返回 null；这里是防御性处理，避免异常数据把整个看板接口打挂。
        if (item != null && OVERVIEW_METRIC_NAMES.containsKey(item.getCode())) {
          countByCode.put(item.getCode(), item);
        }
      }
    }

    // 结果容量固定为四类资源，避免 ArrayList 扩容。
    List<OverviewMetricVO> result = new ArrayList<>(OVERVIEW_METRIC_NAMES.size());
    for (Map.Entry<String, String> entry : OVERVIEW_METRIC_NAMES.entrySet()) {
      // 某个 SQL 分支缺失时也返回该卡片，前端无需自己补默认值。
      OverviewMetricCountDTO count = countByCode.get(entry.getKey());
      result.add(new OverviewMetricVO(
        entry.getKey(),
        // name：统一展示文案，减少不同前端页面重复维护。
        entry.getValue(),
        // total：null 或异常负数统一归零，避免页面展示脏数据。
        normalizeCount(count == null ? null : count.getTotal()),
        // todayIncrease：同样做归零保护；它表示今日创建量，不表示净增长。
        normalizeCount(count == null ? null : count.getTodayIncrease())
      ));
    }
    // 返回不可变副本，避免后续调用方误修改 Service 组装好的结果。
    return List.copyOf(result);
  }

  @Override
  public List<DistributionItemVO> queryAgentModeDistribution(Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");

    List<AgentModeCountDTO> databaseCounts =
      dataDashboardMapper.selectAgentModeCounts(tenantId);

    List<AgentModeCountDTO> validCounts = (databaseCounts == null ? List.<AgentModeCountDTO>of() : databaseCounts).stream()
      .filter(Objects::nonNull)
      .filter(item -> AGENT_MODE_NAMES.containsKey(item.getCode()))
      .filter(item -> normalizeCount(item.getCount()) > 0L)
      .toList();

    long total = validCounts.stream()
      .map(AgentModeCountDTO::getCount)
      .mapToLong(DataDashboardServiceImpl::normalizeCount)
      .sum();

    Map<String, Long> countByMode = new HashMap<>();
    for (AgentModeCountDTO item : validCounts) {
      countByMode.merge(item.getCode(), normalizeCount(item.getCount()), Long::sum);
    }

    List<DistributionItemVO> result = new ArrayList<>(AGENT_MODE_NAMES.size());
    for (Map.Entry<String, String> entry : AGENT_MODE_NAMES.entrySet()) {
      long count = normalizeCount(countByMode.get(entry.getKey()));
      result.add(new DistributionItemVO(
        entry.getKey(),
        entry.getValue(),
        count,
        calculateRatio(count, total)
      ));
    }

    return List.copyOf(result);
  }

  @Override
  public List<DistributionItemVO> queryKnowledgeFileDistribution(Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
   // 优先查询当前租户的文件类型分布缓存。
   // snapshot 中同时包含缓存数据和“是否需要后台刷新”的状态。
    CacheSnapshot<Map<String, Long>> snapshot = getCachedKnowledgeFileCounts(tenantId);
    if (snapshot != null && !snapshot.value().isEmpty()) {
      if (snapshot.refreshRequired()) {
        knowledgeFileDistributionRefreshService.requestRefresh(tenantId);
      }
      return buildKnowledgeFileDistribution(snapshot.value());
    }

    // 执行到这里说明当前租户没有可用缓存
    Map<String, Long> databaseCounts =
      knowledgeFileDistributionRefreshService.loadCurrentCounts(tenantId);
    return buildKnowledgeFileDistribution(databaseCounts);
  }

  @Override
  public List<ModelUsageDistributionItemVO> queryModelUsageDistribution(Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");

    List<ModelUsageStatDTO> source = dataDashboardMapper.selectModelUsageTotals(tenantId);
    if (source == null || source.isEmpty()) {
      return List.of();
    }

    List<ModelUsageDistributionItemVO> result = new ArrayList<>(source.size());
    for (ModelUsageStatDTO item : source) {
      if (item == null) {
        continue;
      }
      // 调用次数做标准化处理：null 或负数归零
      long count = normalizeCount(item.getCount());
      if (count == 0L) {
        continue;
      }
      String productType = item.getProductType();
      result.add(new ModelUsageDistributionItemVO(productType, null, count));
    }

    return List.copyOf(result);
  }

  @Override
  public TodayDynamicsVO queryTodayDynamics(Long tenantId) {
    // 今日动态必须按租户隔离，tenantId 为空时不允许落到数据库默认全量查询。
    Assert.notNull(tenantId, "租户 ID 不能为空");

    // 统计日期按应用服务器本地日期计算，缓存 key 也按自然日隔离。
    LocalDate statisticsDate = LocalDate.now();
    // 优先读取今日动态缓存，首页重复访问时只需要一次 Redis 查询。
    CacheSnapshot<TodayDynamicsVO> cachedSnapshot =
      getCachedTodayDynamics(tenantId, statisticsDate);
    if (cachedSnapshot != null) {
      // 命中逻辑过期缓存时，先返回旧值，再提交后台刷新。
      if (cachedSnapshot.refreshRequired()) {
        // 后台刷新会被协调器按租户和日期合并，避免并发击穿数据库。
        todayDynamicsRefreshService.requestRefresh(tenantId, statisticsDate);
      }
      // 缓存中的统计结果已经是接口 VO，可直接返回。
      return cachedSnapshot.value();
    }

    // 冷缓存或 Redis 异常时同步查库，保证首次访问仍返回真实统计。
    return todayDynamicsRefreshService.loadCurrentDynamics(tenantId, statisticsDate);
  }

  @Override
  public List<ConversationMessageTrendItemVO> queryConversationMessageTrend(Long tenantId) {
    // 趋势接口必须带租户，避免误扫所有会话消息。
    Assert.notNull(tenantId, "租户 ID 不能为空");

    // 结束日期固定为今天，满足“最近三十天，含当天”的页面口径。
    LocalDate endDate = LocalDate.now();
    // 起始日期向前推 29 天，与今天合计正好 30 个自然日。
    LocalDate startDate = endDate.minusDays(CONVERSATION_TREND_DAYS - 1L);

    // 优先读取 Redis 缓存，命中时首页可以绕开当前/历史消息表聚合，稳定加载耗时。
    CacheSnapshot<List<ConversationMessageTrendCountDTO>> cachedSnapshot =
      getCachedConversationMessageTrend(tenantId, endDate);
    // 命中缓存时直接补齐固定 30 天结果；逻辑过期只触发后台刷新，不阻塞本次响应。
    if (cachedSnapshot != null) {
      if (cachedSnapshot.refreshRequired()) {
        conversationMessageTrendRefreshService.requestRefresh(tenantId, endDate);
      }
      return buildConversationMessageTrend(cachedSnapshot.value(), startDate, endDate, tenantId);
    }

    // 冷缓存必须返回真实数据，因此首次访问同步查库，并尽量写入 Redis 供后续首页请求复用。
    List<ConversationMessageTrendCountDTO> databaseCounts =
      conversationMessageTrendRefreshService.loadCurrentCounts(tenantId, endDate);

    // 将数据库聚合行补齐为固定 30 天趋势序列。
    return buildConversationMessageTrend(databaseCounts, startDate, endDate, tenantId);
  }

  private List<ConversationMessageTrendItemVO> buildConversationMessageTrend(
    List<ConversationMessageTrendCountDTO> databaseCounts,
    LocalDate startDate,
    LocalDate endDate,
    Long tenantId
  ) {
    // 按 LocalDate 建索引，方便后面补齐没有消息的日期。
    Map<LocalDate, Long> countByDate = new HashMap<>();
    // 空库或 Mapper 返回 null 时直接进入补零逻辑。
    if (databaseCounts != null) {
      // 逐行处理数据库聚合结果，避免某一行异常影响整个趋势接口。
      for (ConversationMessageTrendCountDTO item : databaseCounts) {
        // 日期为空的数据没有横轴意义，直接忽略。
        if (item == null || item.getStatDate() == null) {
          continue;
        }
        try {
          // Mapper 返回 yyyy-MM-dd 字符串，Service 转成 LocalDate 后再按日期合并。
          countByDate.merge(
            // 解析统计日期，后续统一用 LocalDate 输出。
            LocalDate.parse(item.getStatDate()),
            // 消息数做 null/负数归零保护，避免异常数据影响折线图。
            normalizeCount(item.getMessageCount()),
            // 理论上一天只会一行；merge 用于兼容不同数据库分支或后续 SQL 调整产生的重复日期。
            Long::sum
          );
        }
        catch (DateTimeParseException e) {
          // 单个异常日期只记录告警，不阻断其他日期展示。
          logger.warn("忽略无法解析的每日对话消息量统计日期，tenantId={}, statDate={}",
            tenantId, item.getStatDate());
        }
      }
    }

    // 结果固定 30 条，前端无需自己补日期或排序。
    List<ConversationMessageTrendItemVO> result = new ArrayList<>(CONVERSATION_TREND_DAYS);
    // 从起始日期递增到今天，保证折线图横轴连续稳定。
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      // 当天没有数据库聚合行时返回 0，避免前端把缺失点当作断线。
      result.add(new ConversationMessageTrendItemVO(date, normalizeCount(countByDate.get(date))));
    }
    // 返回不可变副本，避免调用方误改已补齐的趋势序列。
    return List.copyOf(result);
  }

  /**
   * 尝试读取知识库文件分布缓存。
   * <p>Redis 仅是性能优化组件。任何连接、超时或反序列化异常都不能使看板接口失败，
   * 因此这里捕获运行时异常并返回 null，调用方会同步查询数据库以保证首次返回真实数据。</p>
   */
  private CacheSnapshot<Map<String, Long>> getCachedKnowledgeFileCounts(Long tenantId) {
    try {
      return dataDashboardCache.getKnowledgeFileDistribution(tenantId);
    }
    catch (RuntimeException e) {
      logger.warn("读取知识库文件类型分布缓存失败，转为数据库查询，tenantId={}", tenantId, e);
      return null;
    }
  }

  /**
   * 尝试读取每日对话消息量趋势缓存。
   * <p>Redis 仅用于削峰和保障首页响应速度。缓存异常时返回 null，调用方会同步查询数据库，
   * 确保缓存故障不会影响业务正确性。</p>
   */
  private CacheSnapshot<List<ConversationMessageTrendCountDTO>> getCachedConversationMessageTrend(
    Long tenantId,
    LocalDate endDate
  ) {
    try {
      return dataDashboardCache.getConversationMessageTrend(tenantId, endDate);
    }
    catch (RuntimeException e) {
      logger.warn("读取每日对话消息量趋势缓存失败，转为数据库查询，tenantId={}, endDate={}",
        tenantId, endDate, e);
      return null;
    }
  }

  /**
   * 尝试读取今日动态缓存。
   * <p>Redis 仅用于削峰和保障首页响应速度。缓存异常时返回 null，调用方会同步查询数据库，
   * 确保缓存故障不会影响业务正确性。</p>
   */
  private CacheSnapshot<TodayDynamicsVO> getCachedTodayDynamics(
    Long tenantId,
    LocalDate statisticsDate
  ) {
    try {
      return dataDashboardCache.getTodayDynamics(tenantId, statisticsDate);
    }
    catch (RuntimeException e) {
      logger.warn("读取今日动态缓存失败，转为数据库查询，tenantId={}, statisticsDate={}",
        tenantId, statisticsDate, e);
      return null;
    }
  }

  /**
   * 组装知识库文件分布接口结果。
   *
   * <p>缓存可能来自旧请求或被异常数据污染，因此这里重新按固定分类读取并把 null/负数归零。
   * 有数据时采用最大余数法分配百分比，确保所有文件大类比例在保留两位小数后严格合计 100.00；
   * 无数据时所有文件大类均返回 0.00，不人为制造占比。</p>
   */
  private List<DistributionItemVO> buildKnowledgeFileDistribution(Map<String, Long> sourceCounts) {
    List<String> codes = new ArrayList<>(KNOWLEDGE_FILE_CATEGORY_NAMES.keySet());
    List<Long> counts = codes.stream()
      .map(code -> normalizeCount(sourceCounts.get(code)))
      .toList();
    // 根据各类型数量计算百分比，并保证比例总和为 100
    List<BigDecimal> ratios = calculateRatiosSummingToHundred(counts);

    List<DistributionItemVO> result = new ArrayList<>(codes.size());
    for (int i = 0; i < codes.size(); i++) {
      String code = codes.get(i);
      result.add(new DistributionItemVO(
        code,
        KNOWLEDGE_FILE_CATEGORY_NAMES.get(code),
        counts.get(i),
        ratios.get(i)
      ));
    }
    return List.copyOf(result);
  }

  /**
   * 使用最大余数法计算合计严格为 100.00 的百分比。
   *
   * <p>如果直接对每项独立四舍五入，三等分会得到 33.33 + 33.33 + 33.33 = 99.99。
   * 本方法先把每项向下保留两位，再把剩余的 0.01 依次分配给原始小数余数最大的分类。
   * 余数相同时按页面固定分类顺序分配，保证相同数据每次返回完全一致。</p>
   */
  private List<BigDecimal> calculateRatiosSummingToHundred(List<Long> counts) {
    long total = counts.stream().mapToLong(Long::longValue).sum();
    if (total == 0L) {
      return Collections.nCopies(counts.size(), BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));
    }

    List<BigDecimal> ratios = new ArrayList<>(counts.size());
    List<BigDecimal> remainders = new ArrayList<>(counts.size());
    BigDecimal floorSum = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);

    for (long count : counts) {
      BigDecimal exactRatio = BigDecimal.valueOf(count)
        .multiply(ONE_HUNDRED)
        .divide(BigDecimal.valueOf(total), RATIO_INTERMEDIATE_SCALE, RoundingMode.HALF_UP);
      BigDecimal floorRatio = exactRatio.setScale(2, RoundingMode.DOWN);
      ratios.add(floorRatio);
      remainders.add(exactRatio.subtract(floorRatio));
      floorSum = floorSum.add(floorRatio);
    }

    int remainingHundredths = ONE_HUNDRED_WITH_SCALE
      .subtract(floorSum)
      .movePointRight(2)
      .intValueExact();

    List<Integer> indexesByRemainder = new ArrayList<>(counts.size());
    for (int i = 0; i < counts.size(); i++) {
      indexesByRemainder.add(i);
    }
    indexesByRemainder.sort(
      Comparator.comparing((Integer index) -> remainders.get(index))
        .reversed()
        .thenComparingInt(Integer::intValue)
    );

    for (int i = 0; i < remainingHundredths; i++) {
      int index = indexesByRemainder.get(i % indexesByRemainder.size());
      ratios.set(index, ratios.get(index).add(ONE_HUNDREDTH));
    }
    return List.copyOf(ratios);
  }

  /**
   * 计算单项百分比并保留两位小数。
   *
   * <p>智能体模式接口保持常规四舍五入口径；分母为 0 时返回 0.00，避免 null、NaN
   * 或除零异常。知识库文件接口因产品要求合计严格等于 100%，使用单独的最大余数法。</p>
   */
  private BigDecimal calculateRatio(long count, long total) {
    if (total == 0L) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
    }
    return BigDecimal.valueOf(count)
      .multiply(ONE_HUNDRED)
      .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
  }

  /** 将 Mapper/缓存中的 null 或异常负数统一归零，避免产生负占比。 */
  private static long normalizeCount(Long count) {
    return count == null ? 0L : Math.max(count, 0L);
  }

  private static Map<String, String> createAgentModeNames() {
    Map<String, String> names = new LinkedHashMap<>();
    names.put(SceneConsts.SCENE_TYPE_SCENE, "自主规划模式");
    names.put(SceneConsts.SCENE_TYPE_CHATFLOW, "流程编排模式");
    names.put(SceneConsts.SCENE_TYPE_KNOWLEDGE, "知识问答模式");
    return Collections.unmodifiableMap(names);
  }

  private static Map<String, String> createOverviewMetricNames() {
    Map<String, String> names = new LinkedHashMap<>();
    names.put("agent", "智能体总数");
    names.put("knowledge", "知识库总数");
    // 模型来自 bt_library_large_model，统计租户自建和租户可见的平台公开模型。
    names.put("model", "接入大模型数");
    // 工具能力合并插件和 MCP 两类资源，符合页面“插件/MCP”卡片展示。
    names.put("tool", "插件/MCP总数");
    // 对外暴露只读 Map，防止运行中被误改导致卡片顺序或文案变化。
    return Collections.unmodifiableMap(names);
  }

  private static Map<String, String> createKnowledgeFileCategoryNames() {
    Map<String, String> names = new LinkedHashMap<>();
    names.put("document", "文档");
    names.put("spreadsheet", "表格");
    names.put("image", "图片");
    names.put("audioVideo", "音视频");
    names.put(CATEGORY_OTHER, "其他");
    return Collections.unmodifiableMap(names);
  }
}
