package com.iwhalecloud.bote.service.dashboard.impl;

import com.iwhalecloud.bote.cache.DataDashboardCache;
import com.iwhalecloud.bote.cache.DataDashboardCache.CacheSnapshot;
import com.iwhalecloud.bote.dto.dashboard.AgentModeCountDTO;
import com.iwhalecloud.bote.dto.dashboard.DistributionItemVO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageDistributionItemVO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageStatDTO;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricCountDTO;
import com.iwhalecloud.bote.dto.dashboard.OverviewMetricVO;
import com.iwhalecloud.bote.mapper.dashboard.DataDashboardMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 数据概览看板服务单元测试。
 *
 * <p>除智能体模式原有口径外，重点验证知识库文件类型接口的固定分类补零、占比补差、
 * 缓存命中、冷缓存同步加载以及 Redis 异常降级，保证首次访问不会返回伪造的全零数据。</p>
 *
 * @author zhengxueli
 * @since 2026-08-28
 */
@ExtendWith(MockitoExtension.class)
class DataDashboardServiceImplTest {

  private static final Long TENANT_ID = 10001L;

  @Mock
  private DataDashboardMapper dataDashboardMapper;

  @Mock
  private DataDashboardCache dataDashboardCache;

  @Mock
  private KnowledgeFileDistributionRefreshService knowledgeFileDistributionRefreshService;

  @Mock
  private OverviewMetricsRefreshService overviewMetricsRefreshService;

  @Mock
  private ConversationMessageTrendRefreshService conversationMessageTrendRefreshService;

  @Mock
  private TodayDynamicsRefreshService todayDynamicsRefreshService;

  private DataDashboardServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new DataDashboardServiceImpl(
      dataDashboardMapper,
      dataDashboardCache,
      knowledgeFileDistributionRefreshService,
      overviewMetricsRefreshService,
      conversationMessageTrendRefreshService,
      todayDynamicsRefreshService
    );
  }

  @Test
  void returnsOverviewMetricsInFixedDisplayOrderAfterColdCacheLoad() {
    when(dataDashboardCache.getOverviewMetrics(any(), any())).thenReturn(null);
    when(overviewMetricsRefreshService.loadCurrentMetrics(any(), any())).thenReturn(List.of(
      overview("tool", 12L, 2L),
      overview("agent", 8L, 1L),
      overview("model", 6L, 0L),
      overview("knowledge", 4L, 3L)
    ));

    List<OverviewMetricVO> result = service.queryOverviewMetrics(TENANT_ID);

    assertEquals(List.of("agent", "knowledge", "model", "tool"),
      result.stream().map(OverviewMetricVO::getCode).toList());
    assertEquals(List.of("智能体总数", "知识库总数", "接入大模型数", "插件/MCP总数"),
      result.stream().map(OverviewMetricVO::getName).toList());
    assertEquals(List.of(8L, 4L, 6L, 12L),
      result.stream().map(OverviewMetricVO::getTotal).toList());
    assertEquals(List.of(1L, 3L, 0L, 2L),
      result.stream().map(OverviewMetricVO::getTodayIncrease).toList());
    verify(overviewMetricsRefreshService).loadCurrentMetrics(any(), any());
    verify(overviewMetricsRefreshService, never()).requestRefresh(any(), any());
  }

  @Test
  void fillsMissingOverviewMetricsWithZero() {
    when(dataDashboardCache.getOverviewMetrics(any(), any())).thenReturn(null);
    when(overviewMetricsRefreshService.loadCurrentMetrics(any(), any())).thenReturn(List.of(
      overview("agent", 5L, 1L)
    ));

    List<OverviewMetricVO> result = service.queryOverviewMetrics(TENANT_ID);

    assertEquals(List.of("agent", "knowledge", "model", "tool"),
      result.stream().map(OverviewMetricVO::getCode).toList());
    assertEquals(List.of(5L, 0L, 0L, 0L),
      result.stream().map(OverviewMetricVO::getTotal).toList());
    assertEquals(List.of(1L, 0L, 0L, 0L),
      result.stream().map(OverviewMetricVO::getTodayIncrease).toList());
  }

  @Test
  void normalizesNullAndNegativeOverviewCounts() {
    when(dataDashboardCache.getOverviewMetrics(any(), any())).thenReturn(null);
    when(overviewMetricsRefreshService.loadCurrentMetrics(any(), any())).thenReturn(List.of(
      overview("agent", null, -1L),
      overview("knowledge", -3L, null),
      overview("unknown", 99L, 99L)
    ));

    List<OverviewMetricVO> result = service.queryOverviewMetrics(TENANT_ID);

    assertEquals(List.of(0L, 0L, 0L, 0L),
      result.stream().map(OverviewMetricVO::getTotal).toList());
    assertEquals(List.of(0L, 0L, 0L, 0L),
      result.stream().map(OverviewMetricVO::getTodayIncrease).toList());
  }

  @Test
  void usesOverviewMetricsCacheWithoutQueryingDatabase() {
    when(dataDashboardCache.getOverviewMetrics(any(), any())).thenReturn(
      new CacheSnapshot<>(List.of(
        overview("agent", 9L, 1L),
        overview("knowledge", 8L, 2L),
        overview("model", 7L, 3L),
        overview("tool", 6L, 4L)
      ), false)
    );

    List<OverviewMetricVO> result = service.queryOverviewMetrics(TENANT_ID);

    assertEquals(List.of(9L, 8L, 7L, 6L),
      result.stream().map(OverviewMetricVO::getTotal).toList());
    verify(overviewMetricsRefreshService, never()).loadCurrentMetrics(any(), any());
    verify(overviewMetricsRefreshService, never()).requestRefresh(any(), any());
  }

  @Test
  void returnsStaleOverviewMetricsAndRefreshesAsynchronously() {
    when(dataDashboardCache.getOverviewMetrics(any(), any())).thenReturn(
      new CacheSnapshot<>(List.of(overview("agent", 3L, 1L)), true)
    );

    List<OverviewMetricVO> result = service.queryOverviewMetrics(TENANT_ID);

    assertEquals(List.of(3L, 0L, 0L, 0L),
      result.stream().map(OverviewMetricVO::getTotal).toList());
    verify(overviewMetricsRefreshService).requestRefresh(any(), any());
    verify(overviewMetricsRefreshService, never()).loadCurrentMetrics(any(), any());
  }

  @Test
  void returnsRealOverviewMetricsWhenRedisReadFails() {
    when(dataDashboardCache.getOverviewMetrics(any(), any()))
      .thenThrow(new IllegalStateException("redis unavailable"));
    when(overviewMetricsRefreshService.loadCurrentMetrics(any(), any())).thenReturn(List.of(
      overview("agent", 11L, 2L)
    ));

    List<OverviewMetricVO> result = service.queryOverviewMetrics(TENANT_ID);

    assertEquals(List.of(11L, 0L, 0L, 0L),
      result.stream().map(OverviewMetricVO::getTotal).toList());
    verify(overviewMetricsRefreshService).loadCurrentMetrics(any(), any());
    verify(overviewMetricsRefreshService, never()).requestRefresh(any(), any());
  }

  @Test
  void returnsDatabaseModesWithoutApplyingFixedDisplayOrder() {
    when(dataDashboardMapper.selectAgentModeCounts(TENANT_ID)).thenReturn(List.of(
      count("knowledge", 50L),
      count("scene", 25L),
      count("chatflow", 25L)
    ));

    List<DistributionItemVO> result = service.queryAgentModeDistribution(TENANT_ID);

    assertEquals(List.of("scene", "chatflow", "knowledge"),
      result.stream().map(DistributionItemVO::getCode).toList());
    assertEquals(List.of(
        new BigDecimal("25.00"),
        new BigDecimal("25.00"),
        new BigDecimal("50.00")
      ),
      result.stream().map(DistributionItemVO::getRatio).toList());
  }

  @Test
  void fillsModesMissingFromDatabaseResult() {
    when(dataDashboardMapper.selectAgentModeCounts(TENANT_ID)).thenReturn(List.of(count("scene", 10L)));

    List<DistributionItemVO> result = service.queryAgentModeDistribution(TENANT_ID);

    assertEquals(List.of("scene", "chatflow", "knowledge"),
      result.stream().map(DistributionItemVO::getCode).toList());
    assertEquals(List.of(10L, 0L, 0L), result.stream().map(DistributionItemVO::getCount).toList());
    assertEquals(List.of(new BigDecimal("100.00"), new BigDecimal("0.00"), new BigDecimal("0.00")),
      result.stream().map(DistributionItemVO::getRatio).toList());
  }

  @Test
  void returnsZeroModesWhenDatabaseHasNoData() {
    when(dataDashboardMapper.selectAgentModeCounts(TENANT_ID)).thenReturn(List.of());

    List<DistributionItemVO> result = service.queryAgentModeDistribution(TENANT_ID);

    assertEquals(List.of("scene", "chatflow", "knowledge"),
      result.stream().map(DistributionItemVO::getCode).toList());
    assertEquals(List.of(0L, 0L, 0L), result.stream().map(DistributionItemVO::getCount).toList());
  }

  @Test
  void ignoresA2aAndClawModesInModeDistribution() {
    when(dataDashboardMapper.selectAgentModeCounts(TENANT_ID)).thenReturn(List.of(
      count("scene", 2L),
      count("a2a", 3L),
      count("claw", 5L)
    ));

    List<DistributionItemVO> result = service.queryAgentModeDistribution(TENANT_ID);

    assertEquals(List.of("scene", "chatflow", "knowledge"),
      result.stream().map(DistributionItemVO::getCode).toList());
    assertEquals(List.of("自主规划模式", "流程编排模式", "知识问答模式"),
      result.stream().map(DistributionItemVO::getName).toList());
    assertEquals(List.of(
        new BigDecimal("100.00"),
        new BigDecimal("0.00"),
        new BigDecimal("0.00")
      ),
      result.stream().map(DistributionItemVO::getRatio).toList());
  }

  @Test
  void returnsFiveKnowledgeFileCategoriesAndMakesRatiosSumToOneHundred() {
    when(dataDashboardCache.getKnowledgeFileDistribution(TENANT_ID)).thenReturn(
      snapshot(Map.of("document", 1L, "spreadsheet", 1L, "image", 1L, "audioVideo", 0L, "other", 0L), false)
    );

    List<DistributionItemVO> result = service.queryKnowledgeFileDistribution(TENANT_ID);

    assertEquals(List.of("document", "spreadsheet", "image", "audioVideo", "other"),
      result.stream().map(DistributionItemVO::getCode).toList());
    assertEquals(List.of(1L, 1L, 1L, 0L, 0L),
      result.stream().map(DistributionItemVO::getCount).toList());
    assertEquals(List.of(
        new BigDecimal("33.34"),
        new BigDecimal("33.33"),
        new BigDecimal("33.33"),
        new BigDecimal("0.00"),
        new BigDecimal("0.00")
      ),
      result.stream().map(DistributionItemVO::getRatio).toList());
    assertEquals(new BigDecimal("100.00"), result.stream()
      .map(DistributionItemVO::getRatio)
      .reduce(BigDecimal.ZERO, BigDecimal::add));
    verify(knowledgeFileDistributionRefreshService, never()).requestRefresh(TENANT_ID);
  }

  @Test
  void usesKnowledgeFileCacheWithoutQueryingDatabase() {
    when(dataDashboardCache.getKnowledgeFileDistribution(TENANT_ID)).thenReturn(snapshot(Map.of(
      "document", 6L, "spreadsheet", 2L, "image", 1L, "audioVideo", 1L, "other", 0L
    ), false));

    List<DistributionItemVO> result = service.queryKnowledgeFileDistribution(TENANT_ID);

    assertEquals(List.of(6L, 2L, 1L, 1L, 0L),
      result.stream().map(DistributionItemVO::getCount).toList());
    assertEquals(List.of(
        new BigDecimal("60.00"),
        new BigDecimal("20.00"),
        new BigDecimal("10.00"),
        new BigDecimal("10.00"),
        new BigDecimal("0.00")
      ),
      result.stream().map(DistributionItemVO::getRatio).toList());
    verify(dataDashboardMapper, never()).selectKnowledgeFileTypeCounts(TENANT_ID);
    verify(knowledgeFileDistributionRefreshService, never()).requestRefresh(TENANT_ID);
  }

  @Test
  void returnsStaleValueAndRefreshesAsynchronously() {
    when(dataDashboardCache.getKnowledgeFileDistribution(TENANT_ID)).thenReturn(snapshot(Map.of(
      "document", 4L, "spreadsheet", 3L, "image", 2L, "audioVideo", 1L, "other", 0L
    ), true));

    List<DistributionItemVO> result = service.queryKnowledgeFileDistribution(TENANT_ID);

    assertEquals(List.of(4L, 3L, 2L, 1L, 0L),
      result.stream().map(DistributionItemVO::getCount).toList());
    verify(knowledgeFileDistributionRefreshService).requestRefresh(TENANT_ID);
    verify(dataDashboardMapper, never()).selectKnowledgeFileTypeCounts(TENANT_ID);
  }

  @Test
  void returnsRealDatabaseCountsWhenRedisReadFails() {
    when(dataDashboardCache.getKnowledgeFileDistribution(TENANT_ID))
      .thenThrow(new IllegalStateException("redis unavailable"));
    when(knowledgeFileDistributionRefreshService.loadCurrentCounts(TENANT_ID)).thenReturn(Map.of(
      "document", 8L, "spreadsheet", 3L, "image", 2L, "audioVideo", 1L, "other", 0L
    ));

    List<DistributionItemVO> result = service.queryKnowledgeFileDistribution(TENANT_ID);

    assertEquals(List.of(8L, 3L, 2L, 1L, 0L),
      result.stream().map(DistributionItemVO::getCount).toList());
    verify(knowledgeFileDistributionRefreshService).loadCurrentCounts(TENANT_ID);
    verify(knowledgeFileDistributionRefreshService, never()).requestRefresh(TENANT_ID);
  }

  @Test
  void returnsRealDatabaseCountsSynchronouslyOnColdCache() {
    when(dataDashboardCache.getKnowledgeFileDistribution(TENANT_ID)).thenReturn(null);
    when(knowledgeFileDistributionRefreshService.loadCurrentCounts(TENANT_ID)).thenReturn(Map.of(
      "document", 5L, "spreadsheet", 2L, "image", 1L, "audioVideo", 0L, "other", 0L
    ));

    List<DistributionItemVO> result = service.queryKnowledgeFileDistribution(TENANT_ID);

    assertEquals(List.of(5L, 2L, 1L, 0L, 0L),
      result.stream().map(DistributionItemVO::getCount).toList());
    verify(knowledgeFileDistributionRefreshService).loadCurrentCounts(TENANT_ID);
    verify(knowledgeFileDistributionRefreshService, never()).requestRefresh(TENANT_ID);
  }

  @Test
  void returnsModelUsageGroupedByProductTypeWithoutTranslatingName() {
    when(dataDashboardMapper.selectModelUsageTotals(TENANT_ID)).thenReturn(List.of(
      model(null, null, "1100", 150L, null),
      model(null, null, null, 60L, null),
      model(null, null, "1400", 25L, null)
    ));

    List<ModelUsageDistributionItemVO> result =
      service.queryModelUsageDistribution(TENANT_ID);

    assertEquals(Arrays.asList("1100", null, "1400"),
      result.stream().map(ModelUsageDistributionItemVO::getCode).toList());
    assertEquals(Arrays.asList(null, null, null),
      result.stream().map(ModelUsageDistributionItemVO::getName).toList());
    assertEquals(List.of(150L, 60L, 25L),
      result.stream().map(ModelUsageDistributionItemVO::getCount).toList());
  }

  @Test
  void returnsEmptyModelDistributionWhenNoAggregatedUsageExists() {
    when(dataDashboardMapper.selectModelUsageTotals(TENANT_ID)).thenReturn(List.of());

    assertEquals(List.of(), service.queryModelUsageDistribution(TENANT_ID));
  }

  private static AgentModeCountDTO count(String code, Long count) {
    AgentModeCountDTO item = new AgentModeCountDTO();
    item.setCode(code);
    item.setCount(count);
    return item;
  }

  private static ModelUsageStatDTO model(
    Long modelId, String name, String productType, Long count, Integer separatelyDisplayable
  ) {
    ModelUsageStatDTO item = new ModelUsageStatDTO();
    item.setModelId(modelId);
    item.setModelName(name);
    item.setProductType(productType);
    item.setCount(count);
    item.setSeparatelyDisplayable(separatelyDisplayable);
    return item;
  }

  private static OverviewMetricCountDTO overview(String code, Long total, Long todayIncrease) {
    OverviewMetricCountDTO item = new OverviewMetricCountDTO();
    item.setCode(code);
    item.setTotal(total);
    item.setTodayIncrease(todayIncrease);
    return item;
  }

  private static CacheSnapshot<Map<String, Long>> snapshot(
    Map<String, Long> counts,
    boolean refreshRequired
  ) {
    return new CacheSnapshot<>(counts, refreshRequired);
  }
}
