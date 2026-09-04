package com.iwhalecloud.bote.loop.evaluation.domain.event.impl;

import com.iwhalecloud.bote.loop.client.evaluation.expt.ExperimentApplicationService;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpsertExptTurnResultFilterRequest;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UpdateExptAggrResultParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.AggrCalculateEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.CalculateMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptTurnResultFilterEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.UpsertExptTurnResultFilterType;
import com.iwhalecloud.bote.loop.evaluation.domain.event.ExptEventPublisher;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptAggrResultService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptItemEvalEventService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptSchedulerEventService;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExptEventPublisherImpl implements ExptEventPublisher {

  private ExptSchedulerEventService exptSchedulerEventService;
  private ExptItemEvalEventService exptItemEvalEventService;
  private ExperimentApplicationService experimentApplicationService;
  private ExptAggrResultService exptAggrResultService;

  public ExptSchedulerEventService getExptSchedulerEventService() {
    if (exptSchedulerEventService == null) {
      exptSchedulerEventService = SpringUtil.getBean(ExptSchedulerEventService.class);
    }
    return exptSchedulerEventService;
  }

  public ExptItemEvalEventService getExptItemEvalEventService() {
    if (exptItemEvalEventService == null) {
      exptItemEvalEventService = SpringUtil.getBean(ExptItemEvalEventService.class);
    }
    return exptItemEvalEventService;
  }

  public ExperimentApplicationService getExperimentApplicationService() {
    if (experimentApplicationService == null) {
      experimentApplicationService = SpringUtil.getBean(ExperimentApplicationService.class);
    }
    return experimentApplicationService;
  }

  public ExptAggrResultService getExptAggrResultService() {
    if (exptAggrResultService == null) {
      exptAggrResultService = SpringUtil.getBean(ExptAggrResultService.class);
    }
    return exptAggrResultService;
  }


  @Override
  public void publishExptScheduleEvent(ExptScheduleEvent event, Duration duration) {
    getExptSchedulerEventService().schedule(event);
  }

  @Override
  public void publishExptRecordEvalEvent(ExptItemEvalEvent event, Duration duration) {
    getExptItemEvalEventService().eval(event);
  }

  @Override
  public void batchPublishExptRecordEvalEvent(List<ExptItemEvalEvent> events, Duration duration) {
    for (ExptItemEvalEvent event : events) {
      getExptItemEvalEventService().eval(event);
    }
  }

  @Override
  public void publishExptAggrCalculateEvent(List<AggrCalculateEvent> events, Duration duration) {
    for (AggrCalculateEvent event : events) {
      long spaceId = event.getSpaceId();
      long experimentId = event.getExperimentId();
      FieldType fieldType = event.getFieldType();
      String fieldKey = event.getFieldKey();
      switch (event.getCalculateMode()) {
        case CalculateMode.CREATE_ALL_FIELDS -> getExptAggrResultService().createExptAggrResult(spaceId, experimentId);
        case CalculateMode.UPDATE_SPECIFIC_FIELD -> {
          UpdateExptAggrResultParam param = new UpdateExptAggrResultParam();
          param.setSpaceId(spaceId);
          param.setExperimentId(experimentId);
          param.setFieldType(fieldType);
          param.setFieldKey(fieldKey);
          getExptAggrResultService().updateExptAggrResult(param);
        }
        default -> {
          // 其他事件类型暂不处理
        }
      }
    }
  }

  @Override
  public void publishExptTurnResultFilterEvent(ExptTurnResultFilterEvent event, Duration duration) {
    UpsertExptTurnResultFilterRequest upsertExptTurnResultFilterRequest = new UpsertExptTurnResultFilterRequest();

    upsertExptTurnResultFilterRequest.setWorkspaceId(event.getSpaceId());
    upsertExptTurnResultFilterRequest.setExperimentId(event.getExperimentId());
    upsertExptTurnResultFilterRequest.setItemIds(event.getItemId());

    UpsertExptTurnResultFilterType filterType = event.getFilterType();
    if (filterType != null) {
      upsertExptTurnResultFilterRequest.setFilterType(filterType.getValue());
    }
    Integer retryTimes = event.getRetryTimes();
    if (retryTimes != null) {
      upsertExptTurnResultFilterRequest.setRetryTimes(retryTimes);
    }

    getExperimentApplicationService().upsertExptTurnResultFilter(upsertExptTurnResultFilterRequest);
  }
}
