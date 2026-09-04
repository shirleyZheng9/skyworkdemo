package com.iwhalecloud.bote.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataDashboardCacheTest {

  private static final Long TENANT_ID = 10001L;

  @Mock
  private CacheFactory cacheFactory;

  @Mock
  private ICacheClient cacheClient;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private DataDashboardCache cache;

  @BeforeEach
  void setUp() {
    when(cacheFactory.getCacheClient(anyString(), anyString())).thenReturn(cacheClient);
    when(cacheClient.opsForValue()).thenReturn(valueOperations);
    cache = new DataDashboardCache(cacheFactory);
  }

  @Test
  void returnsNullWhenV2KeyIsMissingAfterVersionUpgrade() {
    when(valueOperations.get(TENANT_ID + ":v2")).thenReturn(null);

    DataDashboardCache.CacheSnapshot<Map<String, Long>> snapshot =
      cache.getKnowledgeFileDistribution(TENANT_ID);

    assertNull(snapshot);
  }

  @Test
  void readsV2ValueAndMarksItForAsynchronousRefreshWhenLogicalTimeIsMissing() {
    when(valueOperations.get(TENANT_ID + ":v2")).thenReturn(
      "{\"document\":2,\"spreadsheet\":1,\"image\":0,\"audioVideo\":0,\"other\":0}"
    );

    DataDashboardCache.CacheSnapshot<Map<String, Long>> snapshot =
      cache.getKnowledgeFileDistribution(TENANT_ID);

    assertEquals(Map.of("document", 2L, "spreadsheet", 1L, "image", 0L, "audioVideo", 0L, "other", 0L),
      snapshot.value());
    assertTrue(snapshot.refreshRequired());
  }

  @Test
  void writesLongPhysicalTtlAndFutureLogicalRefreshTime() {
    cache.putKnowledgeFileDistribution(TENANT_ID, Map.of(
      "document", 2L,
      "spreadsheet", 1L,
      "image", 0L,
      "audioVideo", 0L,
      "other", 0L
    ), false);

    ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
    verify(valueOperations).set(
      org.mockito.ArgumentMatchers.eq(TENANT_ID + ":v2"),
      jsonCaptor.capture(),
      ttlCaptor.capture(),
      org.mockito.ArgumentMatchers.eq(TimeUnit.SECONDS)
    );

    Map<String, Long> value = JsonUtil.parseJsonRequired(
      jsonCaptor.getValue(),
      new TypeReference<Map<String, Long>>() {
      }
    );
    assertTrue(value.get("_refreshAfterEpochMillis") > System.currentTimeMillis());
    assertTrue(ttlCaptor.getValue() >= TimeUnit.HOURS.toSeconds(24));

    when(valueOperations.get(TENANT_ID + ":v2")).thenReturn(jsonCaptor.getValue());
    assertFalse(cache.getKnowledgeFileDistribution(TENANT_ID).refreshRequired());
  }
}
