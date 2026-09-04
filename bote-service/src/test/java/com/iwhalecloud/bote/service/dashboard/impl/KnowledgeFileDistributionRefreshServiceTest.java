package com.iwhalecloud.bote.service.dashboard.impl;

import com.iwhalecloud.bote.cache.DataDashboardCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.dashboard.KnowledgeFileTypeCountDTO;
import com.iwhalecloud.bote.mapper.dashboard.DataDashboardMapper;
import com.iwhalecloud.bote.service.dashboard.support.DashboardRefreshCoordinator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeFileDistributionRefreshServiceTest {

  private static final Long TENANT_ID = 10001L;

  @Mock
  private DataDashboardMapper dataDashboardMapper;

  @Mock
  private DataDashboardCache dataDashboardCache;

  @Mock
  private DashboardRefreshCoordinator refreshCoordinator;

  private KnowledgeFileDistributionRefreshService refreshService;

  @BeforeEach
  void setUp() {
    refreshService = new KnowledgeFileDistributionRefreshService(
      dataDashboardMapper,
      dataDashboardCache,
      refreshCoordinator
    );
  }

  @Test
  void aggregatesAndCachesInBackgroundRefreshPath() {
    executeRefreshActionImmediately();
    when(dataDashboardMapper.selectKnowledgeFileTypeCounts(TENANT_ID)).thenReturn(List.of(
      count("document", 4L),
      count("spreadsheet", 2L),
      count("unexpected", 1L)
    ));

    refreshService.requestRefresh(TENANT_ID);

    verify(dataDashboardCache).putKnowledgeFileDistribution(TENANT_ID, Map.of(
      "document", 4L,
      "spreadsheet", 2L,
      "image", 0L,
      "audioVideo", 0L,
      "other", 1L
    ), false);
    verify(refreshCoordinator).requestRefresh(anyString(), eq(TENANT_ID), any(Runnable.class));
  }

  @Test
  void reusesKnowledgeRefreshEventAndExtractsTenant() {
    executeRefreshActionImmediately();
    when(dataDashboardMapper.selectKnowledgeFileTypeCounts(TENANT_ID)).thenReturn(List.of());

    refreshService.onRefresh(null, CacheConsts.CACHE_NAME_KNOWLEDGE,
      List.of(TENANT_ID + ":90001"));

    verify(dataDashboardMapper).selectKnowledgeFileTypeCounts(TENANT_ID);
    verify(dataDashboardCache).putKnowledgeFileDistribution(TENANT_ID, Map.of(
      "document", 0L,
      "spreadsheet", 0L,
      "image", 0L,
      "audioVideo", 0L,
      "other", 0L
    ), true);
  }

  @Test
  void delegatesConcurrencyControlWithoutQueryingInCaller() {
    refreshService.requestRefresh(TENANT_ID);

    verify(dataDashboardMapper, never()).selectKnowledgeFileTypeCounts(TENANT_ID);
    verify(refreshCoordinator).requestRefresh(anyString(), eq(TENANT_ID), any(Runnable.class));
  }

  @Test
  void loadsRealCountsAndCachesThemOnColdCache() {
    when(dataDashboardMapper.selectKnowledgeFileTypeCounts(TENANT_ID)).thenReturn(List.of(
      count("document", 6L),
      count("image", 2L)
    ));

    Map<String, Long> result = refreshService.loadCurrentCounts(TENANT_ID);

    assertEquals(Map.of(
      "document", 6L,
      "spreadsheet", 0L,
      "image", 2L,
      "audioVideo", 0L,
      "other", 0L
    ), result);
    verify(dataDashboardCache).putKnowledgeFileDistribution(TENANT_ID, result, false);
    verify(refreshCoordinator, never()).requestRefresh(anyString(), eq(TENANT_ID), any(Runnable.class));
  }

  @Test
  void stillReturnsRealCountsWhenColdCacheWriteFails() {
    when(dataDashboardMapper.selectKnowledgeFileTypeCounts(TENANT_ID)).thenReturn(List.of(
      count("spreadsheet", 3L)
    ));
    doThrow(new IllegalStateException("redis unavailable"))
      .when(dataDashboardCache)
      .putKnowledgeFileDistribution(eq(TENANT_ID), any(Map.class), eq(false));

    Map<String, Long> result = refreshService.loadCurrentCounts(TENANT_ID);

    assertEquals(Map.of(
      "document", 0L,
      "spreadsheet", 3L,
      "image", 0L,
      "audioVideo", 0L,
      "other", 0L
    ), result);
    verify(dataDashboardMapper).selectKnowledgeFileTypeCounts(TENANT_ID);
  }

  private void executeRefreshActionImmediately() {
    doAnswer(invocation -> {
      Runnable refreshAction = invocation.getArgument(2);
      refreshAction.run();
      return null;
    }).when(refreshCoordinator).requestRefresh(anyString(), eq(TENANT_ID), any(Runnable.class));
  }

  private static KnowledgeFileTypeCountDTO count(String code, Long count) {
    KnowledgeFileTypeCountDTO item = new KnowledgeFileTypeCountDTO();
    item.setCode(code);
    item.setCount(count);
    return item;
  }
}
