package com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOJobProgress;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorGroup;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IO任务增量更新参数
 * 迁移对应关系: Go语言DeltaDatasetIOJob
 * - 功能: IO任务增量更新参数
 * - 字段定义: 各种增量更新字段
 * <p>
 * Java实现说明:
 * - 对应Go的DeltaDatasetIOJob结构体
 * - 使用Java类定义，包含增量更新参数
 * - 提供增量更新功能
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片 -> Java列表
 * - Go指针 -> Java对象引用
 * - Go time.Time -> Java LocalDateTime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeltaDatasetIOJob {

  /**
   * 总数
   * 迁移对应关系: Go语言DeltaDatasetIOJob.Total
   * - 功能: 总数
   * - 类型: Go的*int64对应Java的Long
   * - 用途: 设置总数
   */
  private Long total;

  /**
   * 状态
   * 迁移对应关系: Go语言DeltaDatasetIOJob.Status
   * - 功能: 任务状态
   * - 类型: Go的*string对应Java的String
   * - 用途: 更新状态
   */
  private String status;

  /**
   * 预处理的已处理数量
   * 迁移对应关系: Go语言DeltaDatasetIOJob.PreProcessed
   * - 功能: 预处理的已处理数量
   * - 类型: Go的*int64对应Java的Long
   * - 用途: 乐观锁检查
   */
  private Long preProcessed;

  /**
   * 增量已处理数量
   * 迁移对应关系: Go语言DeltaDatasetIOJob.DeltaProcessed
   * - 功能: 增量已处理数量
   * - 类型: Go的int64对应Java的Long
   * - 用途: 增加已处理数量
   */
  private Long deltaProcessed;

  /**
   * 增量已写入数量
   * 迁移对应关系: Go语言DeltaDatasetIOJob.DeltaAdded
   * - 功能: 增量已写入数量
   * - 类型: Go的int64对应Java的Long
   * - 用途: 增加已写入数量
   */
  private Long deltaAdded;

  /**
   * 子进度信息
   * 迁移对应关系: Go语言DeltaDatasetIOJob.SubProgresses
   * - 功能: 子进度信息
   * - 类型: Go的*string对应Java的String
   * - 用途: 更新子进度
   */
  private List<DatasetIOJobProgress> subProgresses;

  /**
   * 错误信息列表
   * 迁移对应关系: Go语言DeltaDatasetIOJob.Errors
   * - 功能: 错误信息列表
   * - 类型: Go的[]*entity.ItemErrorGroup对应Java的List<ItemErrorGroup>
   * - 用途: 更新错误信息
   */
  private List<ItemErrorGroup> errors;

  /**
   * 开始时间
   * 迁移对应关系: Go语言DeltaDatasetIOJob.StartedAt
   * - 功能: 开始时间
   * - 类型: Go的*time.Time对应Java的LocalDateTime
   * - 用途: 设置开始时间
   */
  private LocalDateTime startedAt;

  /**
   * 结束时间
   * 迁移对应关系: Go语言DeltaDatasetIOJob.EndedAt
   * - 功能: 结束时间
   * - 类型: Go的*time.Time对应Java的LocalDateTime
   * - 用途: 设置结束时间
   */
  private LocalDateTime endedAt;

  /**
   * 转换为更新映射
   * 迁移对应关系: Go语言DeltaDatasetIOJob.toUpdates
   * - 功能: 将增量更新参数转换为更新映射
   * - 返回: 更新映射
   * - 用途: 构建更新SQL
   */
  public java.util.Map<String, Object> toUpdates() {
    java.util.Map<String, Object> updates = new java.util.HashMap<>();
    
    setBasicUpdates(updates);
    setTimestampUpdates(updates);
    setStatusUpdates(updates);
    setProgressUpdates(updates);
    setErrorUpdates(updates);
    setSubProgressUpdates(updates);

    return updates;
  }

  private void setBasicUpdates(java.util.Map<String, Object> updates) {
    updates.put("updated_at", LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
    updates.put("progress_processed", "progress_processed + " + (deltaProcessed != null ? deltaProcessed : 0));
    updates.put("progress_added", "progress_added + " + (deltaAdded != null ? deltaAdded : 0));
  }

  private void setTimestampUpdates(java.util.Map<String, Object> updates) {
    if (startedAt != null) {
      updates.put("started_at", startedAt);
    }
    if (endedAt != null) {
      updates.put("ended_at", endedAt);
    }
  }

  private void setStatusUpdates(java.util.Map<String, Object> updates) {
    if (status != null) {
      updates.put("status", status);
    }
  }

  private void setProgressUpdates(java.util.Map<String, Object> updates) {
    if (total != null) {
      updates.put("progress_total", total);
    }
  }

  private void setErrorUpdates(java.util.Map<String, Object> updates) {
    if (errors != null && !errors.isEmpty()) {
      String serializedErrors = serializeErrors();
      updates.put("errors", serializedErrors);
    }
  }

  private String serializeErrors() {
    try {
      ObjectMapper objectMapper = JsonMapper.builder().build();
      return objectMapper.writeValueAsString(errors);
    }
    catch (Exception e) {
      throw new RuntimeException("marshal errors failed", e);
    }
  }

  private void setSubProgressUpdates(java.util.Map<String, Object> updates) {
    if (subProgresses != null) {
      updates.put("sub_progresses", subProgresses);
    }
  }
}
