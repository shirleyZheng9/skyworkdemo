package com.iwhalecloud.bote.service.dashboard.support;

import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardRefreshCoordinatorTest {

  private static final Long TENANT_ID = 10001L;

  @Mock
  private CacheFactory cacheFactory;

  @Mock
  private ICacheClient cacheClient;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private DashboardRefreshCoordinator coordinator;

  @BeforeEach
  void setUp() {
    when(cacheFactory.getCacheClient(anyString(), anyString())).thenReturn(cacheClient);
    when(cacheClient.opsForValue()).thenReturn(valueOperations);
    coordinator = new DashboardRefreshCoordinator(cacheFactory, new SyncTaskExecutor());
  }

  @Test
  void executesActionAndReleasesLockWhenDistributedLockIsAcquired() {
    when(valueOperations.setIfAbsent(
      eq("knowledgeFileDistribution:" + TENANT_ID),
      anyString(),
      eq(120L),
      eq(TimeUnit.SECONDS)
    )).thenReturn(true);
    AtomicInteger executions = new AtomicInteger();

    coordinator.requestRefresh(
      "knowledgeFileDistribution",
      TENANT_ID,
      executions::incrementAndGet
    );

    assertEquals(1, executions.get());
    verify(cacheClient).execute(
      anyString(),
      eq("knowledgeFileDistribution:" + TENANT_ID),
      anyString()
    );
  }

  @Test
  void skipsActionWhenAnotherInstanceOwnsDistributedLock() {
    when(valueOperations.setIfAbsent(
      eq("knowledgeFileDistribution:" + TENANT_ID),
      anyString(),
      eq(120L),
      eq(TimeUnit.SECONDS)
    )).thenReturn(false);
    Runnable refreshAction = org.mockito.Mockito.mock(Runnable.class);

    coordinator.requestRefresh("knowledgeFileDistribution", TENANT_ID, refreshAction);

    verify(refreshAction, never()).run();
    verify(cacheClient, never()).execute(anyString(), anyString(), anyString());
  }
}
