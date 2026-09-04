package com.iwhalecloud.bote.loop.data.domain.dataset.service;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobRunMessage;

/**
 * 项目快照服务接口
 */
public interface ItemSnapshotService {

  /**
   * 运行快照项目任务
   */
  void runSnapshotItemJob(JobRunMessage msg);
}
