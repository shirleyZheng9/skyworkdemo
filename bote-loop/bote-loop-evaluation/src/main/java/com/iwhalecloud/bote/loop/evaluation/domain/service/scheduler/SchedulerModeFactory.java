package com.iwhalecloud.bote.loop.evaluation.domain.service.scheduler;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

public final class SchedulerModeFactory {
  private SchedulerModeFactory() {
  }

  public static ExptSchedulerMode newSchedulerMode(ExptRunMode mode) {
    switch (mode) {
      case SUBMIT -> {
        return SpringUtil.getBean(ExptSubmitMode.class);
      }
      case FAIL_RETRY, ALL_RETRY, ITEM_RETRY -> {
        return SpringUtil.getBean(ExptRetryMode.class);
      }
      case APPEND -> {
        return SpringUtil.getBean(ExptAppendMode.class);
      }
      default -> {
        return null;
      }
    }
  }

}
