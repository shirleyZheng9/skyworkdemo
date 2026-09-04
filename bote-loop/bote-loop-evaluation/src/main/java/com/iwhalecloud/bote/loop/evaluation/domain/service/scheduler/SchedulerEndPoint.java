package com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;

@FunctionalInterface
public interface SchedulerEndPoint {
  void handle(ExptScheduleEvent event);
}

