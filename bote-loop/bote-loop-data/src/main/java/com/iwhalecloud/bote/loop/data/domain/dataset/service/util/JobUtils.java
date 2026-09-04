package com.iwhalecloud.bote.loop.data.domain.dataset.service.util;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobRunMessage;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobRunType;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.DatasetDomainService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

import java.util.Map;

public final class JobUtils {
  private JobUtils() {

  }
  public static void runSnapshotItemJob(DatasetWithSchema ds, DatasetVersion version) {
    JobRunMessage jobRunMessage = new JobRunMessage();
    jobRunMessage.setType(JobRunType.DATASET_SNAPSHOT_JOB);
    jobRunMessage.setSpaceId(ds.getDataset().getSpaceId());
    jobRunMessage.setExtra(Map.of("version_id", String.valueOf(version.getId())));
    jobRunMessage.setOperator(ds.getDataset().getUpdatedBy());
    // 使用同步调用，避免创建版本失败，数据集快照丢失
    // ThreadPools.getCommon().submit(() -> runSnapshotItemJob(jobRunMessage));
    runSnapshotItemJob(jobRunMessage);
  }

  public static void runSnapshotItemJob(JobRunMessage msg) {
    DatasetDomainService datasetDomainService = SpringUtil.getBean(DatasetDomainService.class);
    datasetDomainService.runSnapshotItemJob(msg);
  }
}
