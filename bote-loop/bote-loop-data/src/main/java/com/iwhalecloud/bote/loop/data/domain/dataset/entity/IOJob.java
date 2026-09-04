package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IO任务实体
 * 迁移对应关系: Go语言entity.IOJob
 * - 功能: 存储IO任务信息
 * - 字段定义:
 * * ID: int64 - 任务ID
 * * AppID: *int32 - 应用ID
 * * SpaceID: int64 - 空间ID
 * * DatasetID: int64 - 数据集ID
 * * JobType: JobType - 任务类型
 * * Source: *DatasetIOEndpoint - 源端点
 * * Target: *DatasetIOEndpoint - 目标端点
 * * FieldMappings: []*FieldMapping - 字段映射
 * * Option: *DatasetIOJobOption - 任务选项
 * * Status: *JobStatus - 状态
 * * Progress: *DatasetIOJobProgress - 进度
 * * Errors: []*ItemErrorGroup - 错误列表
 * * CreatedBy: *string - 创建者
 * * CreatedAt: *int64 - 创建时间
 * * UpdatedBy: *string - 更新者
 * * UpdatedAt: *int64 - 更新时间
 * * StartedAt: *int64 - 开始时间
 * * EndedAt: *int64 - 结束时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.IOJob结构体
 * - 使用Java类定义，包含IO任务字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 包含业务逻辑方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go切片 -> Java List
 * - Go方法 -> Java方法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IOJob {

  /**
   * -- GETTER --
   * 获取ID
   * 迁移对应关系: Go语言entity.IOJob.GetID()
   * - 功能: 获取任务ID
   * - 返回: 任务ID
   */
  @JsonProperty("id")
  private Long id;

  @JsonProperty("app_id")
  private Integer appId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("dataset_id")
  private Long datasetId;

  @JsonProperty("job_type")
  private JobType jobType;

  @JsonProperty("source")
  private DatasetIOEndpoint source;

  @JsonProperty("target")
  private DatasetIOEndpoint target;

  @JsonProperty("field_mappings")
  private List<FieldMapping> fieldMappings;

  @JsonProperty("option")
  private DatasetIOJobOption option;

  @JsonProperty("status")
  private JobStatus status;

  @JsonProperty("progress")
  private DatasetIOJobProgress progress;

  @JsonProperty("errors")
  private List<ItemErrorGroup> errors;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("created_at")
  private Long createdAt;

  @JsonProperty("updated_by")
  private String updatedBy;

  @JsonProperty("updated_at")
  private Long updatedAt;

  @JsonProperty("started_at")
  private Long startedAt;

  @JsonProperty("ended_at")
  private Long endedAt;

  /**
   * 判断是否设置了开始时间
   * 迁移对应关系: Go语言entity.IOJob.IsSetStartedAt()
   * - 功能: 判断是否设置了开始时间
   * - 返回: 是否设置了开始时间
   */
  public boolean isSetStartedAt() {
    return startedAt != null;
  }

  /**
   * 判断是否设置了结束时间
   * 迁移对应关系: Go语言entity.IOJob.IsSetEndedAt()
   * - 功能: 判断是否设置了结束时间
   * - 返回: 是否设置了结束时间
   */
  public boolean isSetEndedAt() {
    return endedAt != null;
  }
}
