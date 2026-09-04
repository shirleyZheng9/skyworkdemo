package com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler;

@FunctionalInterface
public interface SchedulerMiddleware {
  SchedulerEndPoint apply(SchedulerEndPoint next);
}
