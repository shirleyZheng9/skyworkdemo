package com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler;

import java.util.ArrayList;
import java.util.List;

public final class SchedulerChain {

  private final List<SchedulerMiddleware> middlewares = new ArrayList<>();

  private SchedulerChain() {
  }

  public static SchedulerChain.Builder builder() {
    return new SchedulerChain.Builder();
  }

  public static class Builder {
    private final SchedulerChain chain = new SchedulerChain();

    public SchedulerChain.Builder addMiddleware(SchedulerMiddleware middleware) {
      chain.middlewares.add(middleware);
      return this;
    }

    public SchedulerEndPoint build() {
      SchedulerEndPoint next = (event) -> {
      }; // 默认的空实现

      // 从后往前构建链
      for (int i = chain.middlewares.size() - 1; i >= 0; i--) {
        next = chain.middlewares.get(i).apply(next);
      }

      return next;
    }
  }
}
