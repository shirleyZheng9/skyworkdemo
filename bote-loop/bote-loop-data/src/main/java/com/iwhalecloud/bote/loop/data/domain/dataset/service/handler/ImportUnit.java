package com.iwhalecloud.bote.loop.data.domain.dataset.service.handler;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOJobProgress;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorDetail;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.DeltaDatasetIOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.IndexedItem;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * 导入单元
 * 迁移对应关系: Go语言importUnit
 * - 功能: 管理当前批次的处理状态
 * - 字段定义: 各种处理状态字段
 * <p>
 * Java实现说明:
 * - 对应Go的importUnit结构体
 * - 使用Lombok注解简化代码
 * - 提供导入单元管理功能
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片 -> Java List
 * - Go映射 -> Java Map
 * - Go指针 -> Java包装类型
 */
@Data
@SuppressWarnings("PMD.GuardLogStatement")
public class ImportUnit {
  private static final Logger logger = LoggerFactory.getLogger(ImportUnit.class);

  private JobStatus status;
  private Long preProcessed = 0L;
  private Map<ItemErrorType, ItemErrorGroup> errors = new HashMap<>();
  private Map<String, DatasetIOJobProgress> progresses = new HashMap<>();
  private String filename;

  // 以下字段，每次保存后会清空
  private LocalDateTime startedAt;
  private Long total;
  private Long processed = 0L;
  private Long added = 0L;
  private List<IndexedItem> items = new ArrayList<>();

  /**
   * 处理内部错误
   * 迁移对应关系: Go语言onInternalErr
   * - 功能: 处理内部错误
   * - 参数: err - 异常, msg - 消息
   * - 用途: 错误处理
   */
  public void onInternalErr(Exception err, String msg) {
    logger.warn("got internal error '{}', file={}, err={}", msg, filename, err.getMessage());
    appendError(ItemErrorGroup.builder()
      .type(ItemErrorType.INTERNAL_ERROR)
      .details(List.of(ItemErrorDetail.builder()
        .message(msg)
        .build()))
      .build());
  }

  /**
   * 处理数据集容量超限
   * 迁移对应关系: Go语言onDatasetFull
   * - 功能: 处理数据集容量超限
   * - 用途: 容量超限处理
   */
  public void onDatasetFull() {
    appendError(ItemErrorGroup.builder()
      .type(ItemErrorType.EXCEED_DATASET_CAPACITY)
      .details(List.of(ItemErrorDetail.builder()
        .message("exceed dataset capacity")
        .build()))
      .errorCount(-1)
      .build());
    status = JobStatus.COMPLETED; // 超限视作完成，不记作失败
  }

  /**
   * 处理非法内容错误
   * 迁移对应关系: Go语言onIllegalContentErr
   * - 功能: 处理非法内容错误
   * - 参数: err - 异常, msg - 消息
   * - 用途: 非法内容处理
   */
  public void onIllegalContentErr(Exception err, String msg) {
    logger.warn("contain illegal content '{}', file={}, err={}", msg, filename, err.getMessage());
    appendError(ItemErrorGroup.builder()
      .type(ItemErrorType.ILLEGAL_CONTENT)
      .details(List.of(ItemErrorDetail.builder()
        .message(msg)
        .build()))
      .build());
    status = JobStatus.COMPLETED; // 审核不通过视为完成，不记作失败
  }

  /**
   * 处理错误项目
   * 迁移对应关系: Go语言onBadItems
   * - 功能: 处理错误项目
   * - 参数: errors - 错误组列表
   * - 用途: 错误项目处理
   */
  public void onBadItems(ItemErrorGroup... errors) {
    for (ItemErrorGroup e : errors) {
      List<String> details = e.getDetails().stream()
        .map(ItemErrorDetail::getMessage)
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
      logger.info("got {} invalid items, file={}, error_type={}, details={}",
        e.getErrorCount(), filename, e.getType(), details);
      appendError(e);
    }
  }

  /**
   * 添加错误
   * 迁移对应关系: Go语言appendError
   * - 功能: 添加错误
   * - 参数: eg - 错误组
   * - 用途: 错误收集
   */
  private void appendError(ItemErrorGroup eg) {
    if (errors == null) {
      errors = new HashMap<>();
    }

    ItemErrorGroup pre = errors.get(eg.getType());
    if (pre == null) {
      pre = ItemErrorGroup.builder()
        .type(eg.getType())
        .build();
      errors.put(eg.getType(), pre);
    }

    pre.setErrorCount(pre.getErrorCount() + eg.getErrorCount());
    pre.getDetails().addAll(eg.getDetails());
    if (pre.getDetails().size() > 10) {
      pre.setDetails(pre.getDetails().subList(0, 10));
    }
  }

  /**
   * 刷新数据
   * 迁移对应关系: Go语言onFlush
   * - 功能: 刷新数据
   * - 用途: 清理临时数据
   */
  public void onFlush() {
    progresses.put(filename, mergeProgress());
    preProcessed += processed;
    processed = 0L;
    added = 0L;
    items.clear();
    startedAt = null;
    total = null;
  }

  /**
   * 检查是否为空
   * 迁移对应关系: Go语言isEmpty
   * - 功能: 检查是否为空
   * - 返回: 是否为空
   * - 用途: 空值检查
   */
  public boolean isEmpty() {
    return processed <= 0 && added <= 0 && CollectionUtils.isEmpty(items) &&
      CollectionUtils.isEmpty(errors) && status == JobStatus.RUNNING &&
      startedAt == null && total == null;
  }

  /**
   * 合并进度
   * 迁移对应关系: Go语言mergeProgress
   * - 功能: 合并进度
   * - 返回: 合并后的进度
   * - 用途: 进度合并
   */
  private DatasetIOJobProgress mergeProgress() {
    DatasetIOJobProgress prog = DatasetIOJobProgress.builder()
      .name(filename)
      .processed(processed)
      .added(added)
      .total(total)
      .build();

    if (progresses.containsKey(filename)) {
      DatasetIOJobProgress pre = progresses.get(filename);
      prog.setProcessed(pre.getProcessed() + processed);
      prog.setAdded(pre.getAdded() + added);
    }

    return prog;
  }

  /**
   * 转换为增量IO任务
   * 迁移对应关系: Go语言toDeltaDatasetIOJob
   * - 功能: 转换为增量IO任务
   * - 返回: 增量IO任务
   * - 用途: 状态更新
   */
  public DeltaDatasetIOJob toDeltaDatasetIOJob() {
    DatasetIOJobProgress prog = mergeProgress();
    List<DatasetIOJobProgress> progs = progresses.values().stream()
      .filter(p -> !p.getName().equals(filename))
      .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    progs.add(prog);

    DeltaDatasetIOJob delta = DeltaDatasetIOJob.builder()
      .status(status.toString())
      .preProcessed(preProcessed)
      .subProgresses(progs)
      .deltaProcessed(processed)
      .deltaAdded(added)
      .errors(new ArrayList<>(errors.values()))
      .startedAt(startedAt)
      .build();

    if (isJobTerminal(status)) {
      delta.setEndedAt(LocalDateTime.now());
      long total = progs.stream()
        .mapToLong(DatasetIOJobProgress::getTotal)
        .sum();
      delta.setTotal(total);
    }

    return delta;
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
}
