package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录评估链构建器
 * 迁移对应关系: Go语言RecordEvalChain
 * - 功能: 构建中间件链
 */
public final class RecordEvalChain {
  private final List<RecordEvalMiddleware> middlewares = new ArrayList<>();

  private RecordEvalChain() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final RecordEvalChain chain = new RecordEvalChain();

    public Builder addMiddleware(RecordEvalMiddleware middleware) {
      chain.middlewares.add(middleware);
      return this;
    }

    public RecordEvalEndPoint build() {
      RecordEvalEndPoint next = (event) -> {
      }; // 默认的空实现

      // 从后往前构建链
      for (int i = chain.middlewares.size() - 1; i >= 0; i--) {
        next = chain.middlewares.get(i).apply(next);
      }

      return next;
    }
  }
}
