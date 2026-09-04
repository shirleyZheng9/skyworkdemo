package com.iwhalecloud.bote.loop.evaluation.domain.event;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.AggrCalculateEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptTurnResultFilterEvent;
import java.time.Duration;
import java.util.List;

// 对应Go的ExptEventPublisher接口
public interface ExptEventPublisher {
  void publishExptScheduleEvent(
    ExptScheduleEvent event,
    Duration duration
  );

  void publishExptRecordEvalEvent(
    ExptItemEvalEvent event,
    Duration duration
  );

  void batchPublishExptRecordEvalEvent(
    List<ExptItemEvalEvent> events,
    Duration duration
  );

  void publishExptAggrCalculateEvent(
    List<AggrCalculateEvent> events,
    Duration duration
  );

  void publishExptTurnResultFilterEvent(
    ExptTurnResultFilterEvent event,
    Duration duration
  );
}



