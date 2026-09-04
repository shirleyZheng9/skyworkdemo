package com.iwhalecloud.bote.loop.data.domain.dataset.service.impl;


import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemSnapshot;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobRunMessage;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobRunType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SnapshotProgress;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SnapshotStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemSnapshotsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.ItemSnapshotService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.SnapshotContext;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.util.JobUtils;
import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 项目快照服务实现类
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ItemSnapshotServiceImpl implements ItemSnapshotService {
  private static final Logger logger = LoggerFactory.getLogger(ItemSnapshotServiceImpl.class);
  private final IDatasetAPI repo;

  /**
   * 运行快照项目任务
   */
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public void runSnapshotItemJob(JobRunMessage msg) {
    if (msg == null || msg.getType() != JobRunType.DATASET_SNAPSHOT_JOB) {
      logger.warn("message is invalid, msg={}", msg);
      return;
    }
    SnapshotContext processCtx;
    try {
      processCtx = initProgressCtx(msg);
    }
    catch (Exception e) {
      logger.error("init progress context failed", e);
      return;
    }

    if (processCtx.getVersion().getSnapshotStatus().isFinished()) {
      logger.info("snapshot of version is finished, skip. id={}, status={}",
        processCtx.getVersion().getId(), processCtx.getVersion().getSnapshotStatus());
      return;
    }

    boolean ok;
    try {
      ok = checkRetryable(processCtx);
    }
    catch (Exception e) {
      logger.error("check retryable failed", e);
      return;
    }

    if (!ok) {
      logger.info("snapshot version {} failed for exceed max_retry_time", processCtx.getVersionID());
      return;
    }

    try {
      process(processCtx);
    }
    catch (Exception e) {
      logger.error("process snapshot failed", e);
      handleErr(e, processCtx, msg);
    }
  }

  /**
   * 处理快照
   */
  public void process(SnapshotContext processCtx) {
    Boolean isFinished = processCtx.getIsFinished();
    if (isFinished) {
      return;
    }
    DatasetVersion version = processCtx.getVersion();

    ListItemsParams query = new ListItemsParams();
    query.setSpaceId(version.getSpaceId());
    query.setDatasetId(version.getDatasetId());
    query.setAddVnLte(version.getVersionNum());
    query.setDelVnGt(version.getVersionNum());
    Paginator paginator = new Paginator();
    query.setPaginator(paginator);
    int pageSize = 100;
    paginator.setPageSize(pageSize);
    Long countItems = repo.countItems(query);

    for (int i = 0; i < countItems / pageSize + 1; i++) {
      paginator.setPageNum(i + 1);
      PageInfo<Item> items = repo.listItems(query);
      List<ItemSnapshot> snapshots = items.getList().stream()
        .map(item -> {
          ItemSnapshot itemSnapshot = new ItemSnapshot();
          itemSnapshot.setVersionId(version.getId());
          itemSnapshot.setSnapshot(item);
          itemSnapshot.setCreatedAt(new Date());
          return itemSnapshot;
        })
        .collect(Collectors.toList());

      repo.batchUpsertItemSnapshots(snapshots);
    }

    commitProgress(processCtx, false);
  }

  /**
   * 提交进度
   */
  public void commitProgress(SnapshotContext processCtx, boolean hasMore) {
    if (!hasMore) {
      commitToCompleted(processCtx);
      return;
    }

    if (processCtx.getVersion().getSnapshotStatus() == SnapshotStatus.UNSTARTED) {
      commitToInProgress(processCtx);
    }
  }

  /**
   * 提交为完成状态
   */
  public void commitToCompleted(SnapshotContext processCtx) {
    // count total from db to avoid inconsistency
    ListItemSnapshotsParams countParams = new ListItemSnapshotsParams();
    countParams.setSpaceId(processCtx.getSpaceID());
    countParams.setVersionId(processCtx.getVersionID());
    Long total = repo.countItemSnapshots(countParams);

    // update snapshot status to completed
    DatasetVersion patch = buildUpdateVersionPatch(processCtx, SnapshotStatus.COMPLETED, v -> v.setItemCount(total));

    repo.patchVersion(patch, buildUpdateVersionWhere(processCtx));

    processCtx.setIsFinished(true);
    processCtx.getVersion().setSnapshotStatus(SnapshotStatus.COMPLETED);
    processCtx.getVersion().setUpdateVersion(processCtx.getVersion().getUpdateVersion() + 1);
  }

  /**
   * 提交为进行中状态
   */
  public void commitToInProgress(SnapshotContext processCtx) {
    DatasetVersion patch = buildUpdateVersionPatch(processCtx, SnapshotStatus.IN_PROGRESS);
    repo.patchVersion(patch, buildUpdateVersionWhere(processCtx));

    processCtx.getVersion().setSnapshotStatus(SnapshotStatus.IN_PROGRESS);
    processCtx.getVersion().setUpdateVersion(processCtx.getVersion().getUpdateVersion() + 1);
  }

  /**
   * 提交为失败状态
   */
  public void commitToFailed(SnapshotContext processCtx) {
    DatasetVersion patch = buildUpdateVersionPatch(processCtx, SnapshotStatus.FAILED);
    repo.patchVersion(patch, buildUpdateVersionWhere(processCtx));

    processCtx.setIsFinished(true);
    processCtx.getVersion().setSnapshotStatus(SnapshotStatus.FAILED);
    processCtx.getVersion().setUpdateVersion(processCtx.getVersion().getUpdateVersion() + 1);
  }

  /**
   * 检查是否可重试
   */
  public boolean checkRetryable(SnapshotContext processCtx) {
    if (processCtx.getNowRetryTimes() > getMaxRetryTimes()) {
      // exceed max retry time, commit to failed
      commitToFailed(processCtx);
      return false;
    }
    return true;
  }

  /**
   * 初始化进度上下文
   */
  private SnapshotContext initProgressCtx(JobRunMessage msg) {
    Long versionID = getVersionIDFromMsg(msg);
    DatasetVersion version = repo.getVersion(msg.getSpaceId(), versionID);
    Long nowRetryTimes = getRetryTimeFromMsg(msg);

    if (version.getSnapshotProgress() == null) {
      version.setSnapshotProgress(new SnapshotProgress());
    }

    SnapshotContext context = new SnapshotContext();
    context.setSpaceID(version.getSpaceId());
    context.setVersionID(versionID);
    context.setNowRetryTimes(nowRetryTimes);
    context.setIsFinished(false);
    context.setVersion(version);
    return context;
  }

  /**
   * 从消息中获取版本ID
   */
  private Long getVersionIDFromMsg(JobRunMessage msg) {
    if (msg == null) {
      throw new BssException("msg is null");
    }
    String versionIdStr = msg.getExtra().get("version_id");
    if (versionIdStr == null || versionIdStr.isEmpty()) {
      throw new BssException("version_id invalid, msg=" + msg);
    }
    try {
      return Long.parseLong(versionIdStr);
    }
    catch (NumberFormatException e) {
      throw new BssException("version_id invalid, err=" + e.getMessage() + ", msg=" + msg, e);
    }
  }

  /**
   * 从消息中获取重试次数
   */
  private Long getRetryTimeFromMsg(JobRunMessage msg) {
    if (msg == null) {
      throw new BssException("msg is null");
    }
    String retryTimesStr = msg.getExtra().get("retry_times");
    if (retryTimesStr == null || retryTimesStr.isEmpty()) {
      return 0L;
    }
    try {
      return Long.parseLong(retryTimesStr);
    }
    catch (NumberFormatException e) {
      throw new BssException("retry_times invalid, err=" + e.getMessage() + ", msg=" + msg, e);
    }
  }

  /**
   * 处理错误
   */
  private void handleErr(Exception err, SnapshotContext processCtx, JobRunMessage msg) {
    if (err == null) {
      return;
    }

    if (isRetryableErr(err)) {
      // exceed max retry time, commit to failed
      processCtx.setNowRetryTimes(processCtx.getNowRetryTimes() + 1);
      boolean ok = checkRetryable(processCtx);
      if (!ok) {
        return;
      }
      // send retry message
      JobRunMessage jobRunMessage = new JobRunMessage();
      jobRunMessage.setType(JobRunType.DATASET_SNAPSHOT_JOB);
      jobRunMessage.setSpaceId(processCtx.getSpaceID());
      jobRunMessage.setExtra(Map.of(
        "version_id", String.valueOf(processCtx.getVersionID()),
        "retry_times", String.valueOf(processCtx.getNowRetryTimes())
      ));
      jobRunMessage.setOperator(msg.getOperator());
      JobUtils.runSnapshotItemJob(jobRunMessage);
    }
    else {
      logger.error("snapshot version {} failed for un-retryable err, err={}",
        processCtx.getVersionID(), err.getMessage());
    }
  }

  /**
   * 构建更新版本补丁
   */
  @SafeVarargs
  private DatasetVersion buildUpdateVersionPatch(SnapshotContext processCtx,
                                                 SnapshotStatus status,
                                                 java.util.function.Consumer<DatasetVersion>... opts) {
    DatasetVersion patch = new DatasetVersion();
    patch.setSnapshotStatus(status);
    patch.setSnapshotProgress(processCtx.getVersion().getSnapshotProgress());
    patch.setUpdateVersion(processCtx.getVersion().getUpdateVersion() + 1);
    for (java.util.function.Consumer<DatasetVersion> opt : opts) {
      opt.accept(patch);
    }
    return patch;
  }

  /**
   * 构建更新版本条件
   */
  private DatasetVersion buildUpdateVersionWhere(SnapshotContext processCtx) {
    DatasetVersion version = new DatasetVersion();
    version.setId(processCtx.getVersionID());
    version.setSpaceId(processCtx.getSpaceID());
    version.setUpdateVersion(processCtx.getVersion().getUpdateVersion());
    return version;
  }

  /**
   * 检查是否为可重试错误
   */
  private boolean isRetryableErr(Exception err) {
    return err.getMessage().isBlank();
  }

  /**
   * 获取最大重试次数
   */
  private int getMaxRetryTimes() {
    return 3;
  }

}
