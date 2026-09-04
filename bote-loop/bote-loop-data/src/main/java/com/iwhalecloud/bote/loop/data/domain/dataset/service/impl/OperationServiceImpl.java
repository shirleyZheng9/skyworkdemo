package com.iwhalecloud.bote.loop.data.domain.dataset.service.impl;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOperation;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 数据集操作服务实现类
 * 迁移对应关系: Go语言DatasetServiceImpl操作相关方法
 * - 功能: 实现数据集操作相关的业务逻辑
 * - 方法实现: 各种数据集操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetServiceImpl操作相关方法
 * - 使用Repository层实现数据访问
 * - 提供数据集操作控制功能
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class OperationServiceImpl {
  private static final Logger logger = LoggerFactory.getLogger(OperationServiceImpl.class);
  private static final Duration CREATE_VERSION_MAX_WAIT = Duration.ofMinutes(1);
  private static final Duration WRITE_ITEM_MAX_WAIT = Duration.ofMinutes(1);
  private static final Duration UPDATE_SCHEMA_MAX_WAIT = Duration.ofMinutes(1);

  private final IDatasetAPI repo;

  /**
   * 写入项目操作屏障
   * 迁移对应关系: Go语言DatasetServiceImpl.withWriteItemBarrier
   * - 功能: 获取写入项目操作屏障
   * - 参数: datasetId - 数据集ID, itemCount - 项目数量
   * - 返回: 释放函数
   * - 用途: 控制写入项目操作
   */
  public Runnable withWriteItemBarrier(Long datasetId, Long itemCount) {
    waitNoOp(datasetId, List.of(
      DatasetOpType.CREATE_VERSION,
      DatasetOpType.UPDATE_SCHEMA
    ), WRITE_ITEM_MAX_WAIT);
    Duration ttl = Duration.ofMinutes(1);
    if (itemCount > 0) {
      ttl = Duration.ofMinutes(2);
    }
    return withOpBarrier(datasetId, DatasetOpType.WRITE_ITEM, ttl);
  }

  /**
   * 更新模式操作屏障
   * 迁移对应关系: Go语言DatasetServiceImpl.withUpdateSchemaBarrier
   * - 功能: 获取更新模式操作屏障
   * - 参数: datasetId - 数据集ID
   * - 返回: 释放函数
   * - 用途: 控制更新模式操作
   */
  public Runnable withUpdateSchemaBarrier(Long datasetId) {
    waitNoOp(datasetId, List.of(
      DatasetOpType.WRITE_ITEM,
      DatasetOpType.CREATE_VERSION
    ), UPDATE_SCHEMA_MAX_WAIT);

    Duration ttl = Duration.ofMinutes(1);
    return withOpBarrier(datasetId, DatasetOpType.UPDATE_SCHEMA, ttl);
  }

  /**
   * 创建版本操作屏障
   * 迁移对应关系: Go语言DatasetServiceImpl.withCreateVersionBarrier
   * - 功能: 获取创建版本操作屏障
   * - 参数: datasetId - 数据集ID
   * - 返回: 释放函数
   * - 用途: 控制创建版本操作
   */
  public Runnable withCreateVersionBarrier(Long datasetId) {
    waitNoOp(datasetId, List.of(
      DatasetOpType.WRITE_ITEM,
      DatasetOpType.UPDATE_SCHEMA,
      DatasetOpType.CREATE_VERSION
    ), CREATE_VERSION_MAX_WAIT);

    Duration ttl = Duration.ofMinutes(1);
    return withOpBarrier(datasetId, DatasetOpType.CREATE_VERSION, ttl);
  }

  /**
   * 等待无操作
   * 迁移对应关系: Go语言DatasetServiceImpl.waitNoOp
   * - 功能: 等待指定操作类型完成
   * - 参数: datasetId - 数据集ID, opTypes - 操作类型列表, maxWait - 最大等待时间
   * - 用途: 等待操作完成
   */
  private void waitNoOp(Long datasetId, List<DatasetOpType> opTypes, Duration maxWait) {
    long startTime = System.currentTimeMillis();
    long maxWaitMillis = maxWait.toMillis();
    long initialInterval = 50; // 50ms
    long maxInterval = 10000; // 10s

    while (System.currentTimeMillis() - startTime < maxWaitMillis) {
      try {
        Map<DatasetOpType, List<DatasetOperation>> opMap = repo.mGetDatasetOperations(datasetId, opTypes);
        if (opMap.isEmpty()) {
          return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        long sleepTime = Math.min(initialInterval * (long) Math.pow(2, (double) elapsed / 1000), maxInterval);
        if (sleepTime <= 0) {
          sleepTime = 1; // 确保 sleepTime 至少为 1ms
        }
        Thread.sleep(sleepTime);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new BssException("wait no op interrupted", e);
      }
    }

    throw new BssException("timeout waiting for operations to complete");
  }

  /**
   * 操作屏障
   * 迁移对应关系: Go语言DatasetServiceImpl.withOpBarrier
   * - 功能: 创建操作屏障
   * - 参数: datasetId - 数据集ID, opType - 操作类型, ttl - 生存时间
   * - 返回: 释放函数
   * - 用途: 创建操作屏障
   */
  private Runnable withOpBarrier(Long datasetId, DatasetOpType opType, Duration ttl) {
    DatasetOperation op = DatasetOperation.builder()
      .type(opType)
      .ts(LocalDateTime.now())
      .ttl(ttl)
      .build();

    try {
      repo.addDatasetOperation(datasetId, op);
      logger.info("add dataset operation, dataset_id={}, op={}", datasetId, op);
    }
    catch (Exception e) {
      throw new BssException("add dataset operation failed", e);
    }

    return () -> {
      try {
        repo.delDatasetOperation(datasetId, opType, op.getId());
      }
      catch (Exception e) {
        logger.warn("del dataset operation failed, op_id={}, op_type={}, err={}", op.getId(), opType, e.getMessage());
      }
    };
  }
}
