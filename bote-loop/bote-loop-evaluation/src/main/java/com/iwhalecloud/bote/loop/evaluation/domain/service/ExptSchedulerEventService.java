package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptScheduleEvent;

public interface ExptSchedulerEventService {

  void schedule(ExptScheduleEvent event);

}
