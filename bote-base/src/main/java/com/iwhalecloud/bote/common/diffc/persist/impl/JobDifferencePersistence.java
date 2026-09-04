package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bote.mapper.job.JobManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：定时任务
 *
 * @author qian.sisheng
 * @since 2025-11-06
 */
@Component
public final class JobDifferencePersistence extends BaseRootPersistence<JobDTO> {

  public JobDifferencePersistence(JobManageMapper jobManageMapper) {
    setAddConsumer(jobManageMapper::insertJob);
    setModifyConsumer(jobManageMapper::updateJob);
  }

}
