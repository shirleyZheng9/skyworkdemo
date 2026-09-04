package com.iwhalecloud.bote.service.dashboard.impl;

import com.iwhalecloud.bote.dto.dashboard.ModelUsageAggregationCheckpointDTO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageLogIncrementDTO;
import com.iwhalecloud.bote.dto.dashboard.ModelUsageStatusCandidateDTO;
import com.iwhalecloud.bote.mapper.dashboard.ModelUsageAggregationMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelUsageAggregationServiceImplTest {
  @Mock
  private ModelUsageAggregationMapper mapper;

  @Test
  void groupsSameTenantAndModelAndAdvancesCheckpointAfterWritingTotals() {
    Date start = new Date(System.currentTimeMillis() - 60_000L);
    ModelUsageAggregationCheckpointDTO checkpoint = new ModelUsageAggregationCheckpointDTO();
    checkpoint.setTaskCode(ModelUsageAggregationServiceImpl.TASK_CODE);
    checkpoint.setStatisticsStartTime(start);
    checkpoint.setLastCreatedTime(start);
    checkpoint.setLastLogId(0L);
    ModelUsageLogIncrementDTO first = log(1L, 10001L, 10L, "模型A", "KIMI", new Date(start.getTime() + 1));
    ModelUsageLogIncrementDTO second = log(2L, 10001L, 10L, "模型A", "KIMI", new Date(start.getTime() + 2));

    when(mapper.selectCheckpointForUpdate(ModelUsageAggregationServiceImpl.TASK_CODE))
      .thenReturn(checkpoint);
    when(mapper.selectPendingLogs(eq(start), eq(start), eq(0L), any(Date.class), anyInt()))
      .thenReturn(List.of(first, second));
    when(mapper.incrementTotal(eq(10001L), eq(10L), eq("模型A"), eq("KIMI"), eq(2L)))
      .thenReturn(0);

    ModelUsageAggregationServiceImpl service = new ModelUsageAggregationServiceImpl(mapper);

    assertEquals(2, service.aggregateNextBatch());
    verify(mapper).insertTotal(eq(10001L), eq(10L), eq("模型A"), eq("KIMI"), eq(2L));
    verify(mapper).updateCheckpoint(ModelUsageAggregationServiceImpl.TASK_CODE,
      second.getCreatedTime(), second.getLogId());
  }

  @Test
  void ignoresUnexpectedLogWithoutCreatedTimeAndDoesNotWriteNullCheckpoint() {
    Date start = new Date(System.currentTimeMillis() - 60_000L);
    ModelUsageAggregationCheckpointDTO checkpoint = new ModelUsageAggregationCheckpointDTO();
    checkpoint.setTaskCode(ModelUsageAggregationServiceImpl.TASK_CODE);
    checkpoint.setStatisticsStartTime(start);
    checkpoint.setLastCreatedTime(start);
    checkpoint.setLastLogId(0L);
    ModelUsageLogIncrementDTO invalid = log(1L, 10001L, 10L, "模型A", "KIMI", null);

    when(mapper.selectCheckpointForUpdate(ModelUsageAggregationServiceImpl.TASK_CODE))
      .thenReturn(checkpoint);
    when(mapper.selectPendingLogs(eq(start), eq(start), eq(0L), any(Date.class), anyInt()))
      .thenReturn(List.of(invalid));

    ModelUsageAggregationServiceImpl service = new ModelUsageAggregationServiceImpl(mapper);

    assertEquals(0, service.aggregateNextBatch());
    verify(mapper).selectCheckpointForUpdate(ModelUsageAggregationServiceImpl.TASK_CODE);
    verify(mapper).selectPendingLogs(eq(start), eq(start), eq(0L), any(Date.class), anyInt());
    verifyNoMoreInteractions(mapper);
  }

  @Test
  void persistsAllModelStatusesReturnedByTheBoundedReconciliationQuery() {
    ModelUsageStatusCandidateDTO deleted = new ModelUsageStatusCandidateDTO();
    deleted.setTenantId(10001L);
    deleted.setModelId(10L);
    deleted.setModelStatus("DELETED");
    deleted.setModelSource("UNKNOWN");
    deleted.setProductType(null);
    ModelUsageStatusCandidateDTO disabled = status(10001L, 11L, "DISABLED", "CUSTOM", "KIMI");
    ModelUsageStatusCandidateDTO waiting = status(10001L, 12L, "WAITING", "PLATFORM", "通义千问");
    ModelUsageStatusCandidateDTO unknown = status(10001L, 13L, "UNKNOWN", "PLATFORM", null);
    when(mapper.selectStatusCandidates(anyInt()))
      .thenReturn(List.of(deleted, disabled, waiting, unknown));

    ModelUsageAggregationServiceImpl service = new ModelUsageAggregationServiceImpl(mapper);

    assertEquals(4, service.refreshNextModelStatusBatch());
    verify(mapper).updateModelStatus(10001L, 10L, "DELETED", "UNKNOWN", null);
    verify(mapper).updateModelStatus(10001L, 11L, "DISABLED", "CUSTOM", "KIMI");
    verify(mapper).updateModelStatus(10001L, 12L, "WAITING", "PLATFORM", "通义千问");
    verify(mapper).updateModelStatus(10001L, 13L, "UNKNOWN", "PLATFORM", null);
  }

  private static ModelUsageStatusCandidateDTO status(
    Long tenantId, Long modelId, String modelStatus, String modelSource, String productType
  ) {
    ModelUsageStatusCandidateDTO item = new ModelUsageStatusCandidateDTO();
    item.setTenantId(tenantId);
    item.setModelId(modelId);
    item.setModelStatus(modelStatus);
    item.setModelSource(modelSource);
    item.setProductType(productType);
    return item;
  }

  private static ModelUsageLogIncrementDTO log(
    Long logId, Long tenantId, Long modelId, String modelName, String productType, Date createdTime
  ) {
    ModelUsageLogIncrementDTO item = new ModelUsageLogIncrementDTO();
    item.setLogId(logId);
    item.setTenantId(tenantId);
    item.setModelId(modelId);
    item.setModelName(modelName);
    item.setProductType(productType);
    item.setCreatedTime(createdTime);
    return item;
  }
}
