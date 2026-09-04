package com.iwhalecloud.bote.loop.data.domain.dataset.service.impl;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobRunMessage;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobType;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.DeltaDatasetIOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.IOJobService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.handler.ImportFileServiceImpl;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.handler.ImportHandler;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据集IO任务服务实现类
 * 迁移对应关系: Go语言DatasetServiceImpl IO任务相关方法
 * - 功能: 实现数据集IO任务相关的业务逻辑
 * - 方法实现: 各种数据集IO任务操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetServiceImpl IO任务相关方法
 * - 使用Repository层实现数据访问
 * - 提供数据集IO任务管理功能
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class IOJobServiceImpl implements IOJobService {
  private static final Logger logger = LoggerFactory.getLogger(IOJobServiceImpl.class);

  private final IDatasetAPI repo;
  private final ImportFileServiceImpl importFileService;

  /**
   * 获取IO任务
   * 迁移对应关系: Go语言DatasetServiceImpl.GetIOJob
   * - 功能: 获取IO任务
   * - 参数: jobId - 任务ID
   * - 返回: IO任务对象
   * - 用途: 获取IO任务信息
   */
  public IOJob getIOJob(Long jobId) {
    return repo.getIOJob(jobId);
  }

  /**
   * 创建IO任务
   * 迁移对应关系: Go语言DatasetServiceImpl.CreateIOJob
   * - 功能: 创建IO任务
   * - 参数: job - IO任务对象
   * - 用途: 创建IO任务
   */
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public void createIOJob(IOJob job) {
    repo.createIOJob(job);
    try {
      // TODO: 发送消息到MQ
      logger.info("send dataset_io_job message, job_id={}", job.getId());
    }
    catch (Exception e) {
      logger.error("send dataset_io_job message failed, job_id={}, err={}", job.getId(), e.getMessage());
      repo.updateIOJob(job.getId(), DeltaDatasetIOJob.builder()
        .status(JobStatus.FAILED.getDisplayName())
        .build());
      throw new BssException("send dataset_io_job message failed", e);
    }
  }

  /**
   * 运行IO任务
   * 迁移对应关系: Go语言DatasetServiceImpl.RunIOJob
   * - 功能: 运行IO任务
   * - 参数: msg - 任务运行消息
   * - 用途: 运行IO任务
   */
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public void runIOJob(JobRunMessage msg) {
    IOJob job = repo.getIOJob(msg.getJobId());
    if (job == null) {
      throw new BssException("job not found: " + msg.getJobId());
    }
    if (isJobTerminal(job.getStatus())) {
      logger.info("ignore mq message as job has already ended, job_id={}, status={}", msg.getJobId(), job.getStatus());
      return;
    }
    DatasetWithSchema ds = getDataset(msg.getSpaceId(), job.getDatasetId());
    ds.getDataset().setUpdatedBy(msg.getOperator());
    if (Objects.requireNonNull(job.getJobType()) == JobType.IMPORT_FROM_FILE) {
      handleImportJob(job, ds);
    }
    else {
      throw new BssException("unsupported job type: " + job.getJobType());
    }
  }

  /**
   * 检查任务是否已结束
   * 迁移对应关系: Go语言IsJobTerminal
   * - 功能: 检查任务是否已结束
   * - 参数: status - 任务状态
   * - 返回: 是否已结束
   * - 用途: 任务状态检查
   */
  private boolean isJobTerminal(JobStatus status) {
    return status == JobStatus.COMPLETED || status == JobStatus.FAILED || status == JobStatus.CANCELLED;
  }

  /**
   * 获取数据集
   * 迁移对应关系: Go语言GetDataset调用
   * - 功能: 获取数据集
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID
   * - 返回: 数据集和模式
   * - 用途: 获取数据集信息
   */
  private DatasetWithSchema getDataset(Long spaceId, Long datasetId) {
    return SpringUtil.getBean(DatasetDomainServiceImpl.class).getDataset(spaceId, datasetId);
  }

  /**
   * 处理导入任务
   * 迁移对应关系: Go语言newImportHandler
   * - 功能: 处理导入任务
   * - 参数: job - IO任务, ds - 数据集和模式
   * - 用途: 导入任务处理
   */
  private void handleImportJob(IOJob job, DatasetWithSchema ds) {
    // TODO: 实现导入任务处理逻辑
    logger.info("handling import job, job_id={}, dataset_id={}", job.getId(), ds.getDataset().getId());
    ImportHandler importHandler = importFileService.newImportHandler(job, ds);
    importHandler.handle();
  }
}
