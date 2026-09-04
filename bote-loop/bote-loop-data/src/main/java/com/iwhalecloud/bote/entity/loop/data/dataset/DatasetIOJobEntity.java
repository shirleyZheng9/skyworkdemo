package com.iwhalecloud.bote.entity.loop.data.dataset;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集导入导出任务表持久化对象
 * 迁移对应关系: Go语言model.DatasetIOJob
 * - 功能: 数据集导入导出任务表的数据模型
 * - 表名: bt_dataset_io_job
 * - 字段定义:
 * * id: Long - 主键id
 * * appId: Integer - 应用ID
 * * spaceId: Long - 空间ID
 * * datasetId: Long - 数据集ID
 * * jobType: String - 任务类型
 * * sourceFile: String - 源文件信息
 * * sourceDataset: String - 源数据集信息
 * * targetFile: String - 目标文件信息
 * * targetDataset: String - 目标数据集信息
 * * fieldMappings: String - 字段映射
 * * option: String - 任务选项
 * * status: String - 状态
 * * progressTotal: Long - 总数
 * * progressProcessed: Long - 已处理的数量
 * * progressAdded: Long - 已写入的数量
 * * subProgresses: String - 进度信息
 * * errors: String - 错误信息
 * * createdBy: String - 创建人
 * * createdAt: LocalDateTime - 创建时间
 * * updatedBy: String - 修改人
 * * updatedAt: LocalDateTime - 修改时间
 * * startedAt: LocalDateTime - 开始时间
 * * endedAt: LocalDateTime - 结束时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.DatasetIOJob结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go datatypes.JSON -> Java String
 * - Go *time.Time -> Java LocalDateTime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bt_dataset_io_job")
public class DatasetIOJobEntity {

  /**
   * 主键id
   * 迁移对应关系: Go语言DatasetIOJob.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * 应用ID
   * 迁移对应关系: Go语言DatasetIOJob.AppID
   * - 功能: 应用标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer appId;

  /**
   * 空间ID
   * 迁移对应关系: Go语言DatasetIOJob.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 数据集ID
   * 迁移对应关系: Go语言DatasetIOJob.DatasetID
   * - 功能: 数据集标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long datasetId;

  /**
   * 任务类型
   * 迁移对应关系: Go语言DatasetIOJob.JobType
   * - 功能: 任务类型
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String jobType;

  /**
   * 源文件信息
   * 迁移对应关系: Go语言DatasetIOJob.SourceFile
   * - 功能: 源文件信息
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String sourceFile;

  /**
   * 源数据集信息
   * 迁移对应关系: Go语言DatasetIOJob.SourceDataset
   * - 功能: 源数据集信息
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String sourceDataset;

  /**
   * 目标文件信息
   * 迁移对应关系: Go语言DatasetIOJob.TargetFile
   * - 功能: 目标文件信息
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String targetFile;

  /**
   * 目标数据集信息
   * 迁移对应关系: Go语言DatasetIOJob.TargetDataset
   * - 功能: 目标数据集信息
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String targetDataset;

  /**
   * 字段映射
   * 迁移对应关系: Go语言DatasetIOJob.FieldMappings
   * - 功能: 字段映射
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String fieldMappings;

  /**
   * 任务选项
   * 迁移对应关系: Go语言DatasetIOJob.Option
   * - 功能: 任务选项
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String option;

  /**
   * 状态
   * 迁移对应关系: Go语言DatasetIOJob.Status
   * - 功能: 状态
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String status;

  /**
   * 总数
   * 迁移对应关系: Go语言DatasetIOJob.ProgressTotal
   * - 功能: 总数
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long progressTotal;

  /**
   * 已处理的数量
   * 迁移对应关系: Go语言DatasetIOJob.ProgressProcessed
   * - 功能: 已处理的数量
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long progressProcessed;

  /**
   * 已写入的数量
   * 迁移对应关系: Go语言DatasetIOJob.ProgressAdded
   * - 功能: 已写入的数量
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long progressAdded;

  /**
   * 进度信息
   * 迁移对应关系: Go语言DatasetIOJob.SubProgresses
   * - 功能: 进度信息
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String subProgresses;

  /**
   * 错误信息
   * 迁移对应关系: Go语言DatasetIOJob.Errors
   * - 功能: 错误信息
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String errors;

  /**
   * 创建人
   * 迁移对应关系: Go语言DatasetIOJob.CreatedBy
   * - 功能: 创建人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言DatasetIOJob.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private LocalDateTime createdAt;

  /**
   * 修改人
   * 迁移对应关系: Go语言DatasetIOJob.UpdatedBy
   * - 功能: 修改人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String updatedBy;

  /**
   * 修改时间
   * 迁移对应关系: Go语言DatasetIOJob.UpdatedAt
   * - 功能: 记录修改时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private LocalDateTime updatedAt;

  /**
   * 开始时间
   * 迁移对应关系: Go语言DatasetIOJob.StartedAt
   * - 功能: 开始时间
   * - 类型: Go的*time.Time对应Java的LocalDateTime
   * - 数据库: timestamp
   */
  private LocalDateTime startedAt;

  /**
   * 结束时间
   * 迁移对应关系: Go语言DatasetIOJob.EndedAt
   * - 功能: 结束时间
   * - 类型: Go的*time.Time对应Java的LocalDateTime
   * - 数据库: timestamp
   */
  private LocalDateTime endedAt;
}
