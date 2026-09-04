package com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IO任务列表查询参数
 * 迁移对应关系: Go语言ListIOJobsParams
 * - 功能: IO任务列表查询参数
 * - 字段定义: 过滤条件等
 * <p>
 * Java实现说明:
 * - 对应Go的ListIOJobsParams结构体
 * - 使用Java类定义，包含查询参数
 * - 提供参数验证功能
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片 -> Java列表
 * - Go验证标签 -> Java验证注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListIOJobsParams {

  /**
   * 空间ID
   * 迁移对应关系: Go语言ListIOJobsParams.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 验证: 必填且大于0
   * - 用途: 空间过滤
   */
  @NotNull(message = "spaceId is required")
  @Positive(message = "spaceId must be positive")
  private Long spaceId;

  /**
   * 数据集ID
   * 迁移对应关系: Go语言ListIOJobsParams.DatasetID
   * - 功能: 数据集标识
   * - 类型: Go的int64对应Java的Long
   * - 验证: 必填且大于0
   * - 用途: 数据集过滤
   */
  @NotNull(message = "datasetId is required")
  @Positive(message = "datasetId must be positive")
  private Long datasetId;

  /**
   * 任务类型列表
   * 迁移对应关系: Go语言ListIOJobsParams.Types
   * - 功能: 任务类型列表
   * - 类型: Go的[]entity.JobType对应Java的List<JobType>
   * - 用途: 类型过滤
   */
  private List<JobType> types;

  /**
   * 任务状态列表
   * 迁移对应关系: Go语言ListIOJobsParams.Statuses
   * - 功能: 任务状态列表
   * - 类型: Go的[]entity.JobStatus对应Java的List<JobStatus>
   * - 用途: 状态过滤
   */
  private List<JobStatus> statuses;
}
