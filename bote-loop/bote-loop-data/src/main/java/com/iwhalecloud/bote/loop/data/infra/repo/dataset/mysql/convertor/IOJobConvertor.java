package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.convertor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetIOJobEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIODataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOEndpoint;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOFile;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOJobOption;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOJobProgress;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldMapping;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobType;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * IO任务转换器
 * 迁移对应关系: Go语言convertor包中的IOJob转换器
 * - 功能: 提供IO任务DO和PO之间的转换
 * - 方法定义: 各种IO任务转换方法
 * <p>
 * Java实现说明:
 * - 对应Go的convertor包中的IOJob转换器
 * - 使用Java静态方法提供转换功能
 * - 提供IO任务DO和PO之间的转换
 * <p>
 * 技术栈迁移:
 * - Go方法 -> Java静态方法
 * - Go JSON序列化 -> Jackson序列化
 * - Go错误处理 -> Java异常处理
 */
public final class IOJobConvertor {

  private IOJobConvertor() {
    // 工具类，禁止实例化
  }

  private static final ObjectMapper objectMapper = JsonMapper.builder().build();

  /**
   * 将IOJob PO转换为DO
   * 迁移对应关系: Go语言IoJobPO2DO
   * - 功能: 将IO任务PO转换为DO
   * - 参数: po - IO任务PO对象
   * - 返回: IO任务DO对象
   * - 用途: 数据查询后的转换
   */
  public static IOJob ioJobPO2DO(DatasetIOJobEntity po) {
    if (po == null) {
      return null;
    }

    IOJob.IOJobBuilder builder = buildBasicIOJob(po);
    DatasetIOJobProgress progress = setProgressInfo(builder, po);
    setJobTypeAndStatus(builder, po);
    setSourceInfo(builder, po);
    setTargetInfo(builder, po);
    setAdditionalInfo(builder, po, progress);
    setTimestamps(builder, po);

    return builder.build();
  }

  private static IOJob.IOJobBuilder buildBasicIOJob(DatasetIOJobEntity po) {
    return IOJob.builder()
      .id(po.getId())
      .appId(po.getAppId())
      .spaceId(po.getSpaceId())
      .datasetId(po.getDatasetId())
      .source(new DatasetIOEndpoint())
      .target(new DatasetIOEndpoint())
      .createdBy(po.getCreatedBy())
      .createdAt(convertTimestamp(po.getCreatedAt()))
      .updatedBy(po.getUpdatedBy())
      .updatedAt(convertTimestamp(po.getUpdatedAt()));
  }

  private static Long convertTimestamp(LocalDateTime dateTime) {
    return dateTime != null ? dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() : null;
  }

  private static DatasetIOJobProgress setProgressInfo(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    DatasetIOJobProgress progress = DatasetIOJobProgress.builder()
      .total(po.getProgressTotal())
      .processed(po.getProgressProcessed())
      .added(po.getProgressAdded())
      .build();
    builder.progress(progress);
    return progress;
  }

  private static void setJobTypeAndStatus(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    setJobType(builder, po);
    setJobStatus(builder, po);
  }

  private static void setJobType(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    try {
      builder.jobType(JobType.fromString(po.getJobType()));
    }
    catch (Exception e) {
      throw new BssException("unknown job_type '" + po.getJobType() + "'", e);
    }
  }

  private static void setJobStatus(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    try {
      builder.status(JobStatus.fromString(po.getStatus()));
    }
    catch (Exception e) {
      throw new BssException("unknown job_status '" + po.getStatus() + "'", e);
    }
  }

  private static void setSourceInfo(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    setSourceDataset(builder, po);
    setSourceFile(builder, po);
  }

  private static void setSourceDataset(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    if (po.getSourceDataset() != null && !po.getSourceDataset().isEmpty()) {
      try {
        DatasetIODataset sourceDataset = objectMapper.readValue(po.getSourceDataset(), DatasetIODataset.class);
        builder.source(DatasetIOEndpoint.builder().dataset(sourceDataset).build());
      }
      catch (Exception e) {
        throw new BssException("unmarshal source_dataset failed", e);
      }
    }
  }

  private static void setSourceFile(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    if (po.getSourceFile() != null && !po.getSourceFile().isEmpty()) {
      try {
        DatasetIOFile sourceFile = objectMapper.readValue(po.getSourceFile(), DatasetIOFile.class);
        builder.source(DatasetIOEndpoint.builder().file(sourceFile).build());
      }
      catch (Exception e) {
        throw new BssException("unmarshal source_file failed", e);
      }
    }
  }

  private static void setTargetInfo(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    setTargetDataset(builder, po);
    setTargetFile(builder, po);
  }

  private static void setTargetDataset(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    if (po.getTargetDataset() != null && !po.getTargetDataset().isEmpty()) {
      try {
        DatasetIODataset targetDataset = objectMapper.readValue(po.getTargetDataset(), DatasetIODataset.class);
        builder.target(DatasetIOEndpoint.builder().dataset(targetDataset).build());
      }
      catch (Exception e) {
        throw new BssException("unmarshal target_dataset failed", e);
      }
    }
  }

  private static void setTargetFile(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    if (po.getTargetFile() != null && !po.getTargetFile().isEmpty()) {
      try {
        DatasetIOFile targetFile = objectMapper.readValue(po.getTargetFile(), DatasetIOFile.class);
        builder.target(DatasetIOEndpoint.builder().file(targetFile).build());
      }
      catch (Exception e) {
        throw new BssException("unmarshal target_file failed", e);
      }
    }
  }

  private static void setAdditionalInfo(IOJob.IOJobBuilder builder, DatasetIOJobEntity po, DatasetIOJobProgress progress) {
    setFieldMappings(builder, po);
    setOption(builder, po);
    setSubProgresses(progress, po);
    setErrors(builder, po);
  }

  private static void setFieldMappings(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    if (po.getFieldMappings() != null && !po.getFieldMappings().isEmpty()) {
      try {
        List<FieldMapping> fieldMappings = objectMapper.readValue(po.getFieldMappings(), new TypeReference<List<FieldMapping>>() {
        });
        builder.fieldMappings(fieldMappings);
      }
      catch (Exception e) {
        throw new BssException("unmarshal field_mappings failed", e);
      }
    }
  }

  private static void setOption(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    if (po.getOption() != null && !po.getOption().isEmpty()) {
      try {
        DatasetIOJobOption option = objectMapper.readValue(po.getOption(), DatasetIOJobOption.class);
        builder.option(option);
      }
      catch (Exception e) {
        throw new BssException("unmarshal option failed", e);
      }
    }
  }

  private static void setSubProgresses(DatasetIOJobProgress progress, DatasetIOJobEntity po) {
    if (po.getSubProgresses() != null && !po.getSubProgresses().isEmpty()) {
      try {
        List<DatasetIOJobProgress> subProgresses = objectMapper.readValue(po.getSubProgresses(), new TypeReference<List<DatasetIOJobProgress>>() {
        });
        progress.setSubProgresses(subProgresses);
      }
      catch (Exception e) {
        throw new BssException("unmarshal sub_progresses failed", e);
      }
    }
  }

  private static void setErrors(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    if (po.getErrors() != null && !po.getErrors().isEmpty()) {
      try {
        List<ItemErrorGroup> errors = objectMapper.readValue(po.getErrors(), new TypeReference<List<ItemErrorGroup>>() {
        });
        builder.errors(errors);
      }
      catch (Exception e) {
        throw new BssException("unmarshal errors failed", e);
      }
    }
  }

  private static void setTimestamps(IOJob.IOJobBuilder builder, DatasetIOJobEntity po) {
    if (po.getStartedAt() != null) {
      builder.startedAt(po.getStartedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
    }
    if (po.getEndedAt() != null) {
      builder.endedAt(po.getEndedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
    }
  }

  /**
   * 将IOJob DO转换为PO
   * 迁移对应关系: Go语言ConvertIoJobDOToPO
   * - 功能: 将IO任务DO转换为PO
   * - 参数: do - IO任务DO对象
   * - 返回: IO任务PO对象
   * - 用途: 数据持久化前的转换
   */
  public static DatasetIOJobEntity ioJobDO2PO(IOJob ioJob) {
    if (ioJob == null) {
      return null;
    }

    DatasetIOJobEntity.DatasetIOJobEntityBuilder builder = createBaseBuilder(ioJob);
    setProgressInfo(builder, ioJob);
    setSourceInfo(builder, ioJob);
    setTargetInfo(builder, ioJob);
    setAdditionalInfo(builder, ioJob);

    return builder.build();
  }

  private static DatasetIOJobEntity.DatasetIOJobEntityBuilder createBaseBuilder(IOJob ioJob) {
    return DatasetIOJobEntity.builder()
      .id(ioJob.getId())
      .appId(ioJob.getAppId())
      .spaceId(ioJob.getSpaceId())
      .datasetId(ioJob.getDatasetId())
      .jobType(ioJob.getJobType() != null ? ioJob.getJobType().getDisplayName() : null)
      .status(ioJob.getStatus() != null ? ioJob.getStatus().getDisplayName() : null)
      .createdBy(ioJob.getCreatedBy())
      .createdAt(convertTimestamp(ioJob.getCreatedAt()))
      .updatedBy(ioJob.getUpdatedBy())
      .updatedAt(convertTimestamp(ioJob.getUpdatedAt()))
      .startedAt(convertTimestamp(ioJob.getStartedAt()))
      .endedAt(convertTimestamp(ioJob.getEndedAt()));
  }

  public static LocalDateTime convertTimestamp(Long timestamp) {
    return timestamp != null ? LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC) : null;
  }

  private static void setProgressInfo(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getProgress() != null) {
      builder.progressTotal(ioJob.getProgress().getTotal())
        .progressProcessed(ioJob.getProgress().getProcessed())
        .progressAdded(ioJob.getProgress().getAdded());
    }
  }

  private static void setSourceInfo(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getSource() != null) {
      setSourceDataset(builder, ioJob);
      setSourceFile(builder, ioJob);
    }
  }

  private static void setSourceDataset(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getSource().getDataset() != null) {
      try {
        String sourceDatasetJson = objectMapper.writeValueAsString(ioJob.getSource().getDataset());
        builder.sourceDataset(sourceDatasetJson);
      }
      catch (Exception e) {
        throw new BssException("marshal source dataset failed", e);
      }
    }
  }

  private static void setSourceFile(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getSource().getFile() != null) {
      try {
        String sourceFileJson = objectMapper.writeValueAsString(ioJob.getSource().getFile());
        builder.sourceFile(sourceFileJson);
      }
      catch (Exception e) {
        throw new BssException("marshal source file failed", e);
      }
    }
  }

  private static void setTargetInfo(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getTarget() != null) {
      setTargetDataset(builder, ioJob);
      setTargetFile(builder, ioJob);
    }
  }

  private static void setTargetDataset(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getTarget().getDataset() != null) {
      try {
        String targetDatasetJson = objectMapper.writeValueAsString(ioJob.getTarget().getDataset());
        builder.targetDataset(targetDatasetJson);
      }
      catch (Exception e) {
        throw new BssException("marshal target dataset failed", e);
      }
    }
  }

  private static void setTargetFile(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getTarget().getFile() != null) {
      try {
        String targetFileJson = objectMapper.writeValueAsString(ioJob.getTarget().getFile());
        builder.targetFile(targetFileJson);
      }
      catch (Exception e) {
        throw new BssException("marshal target file failed", e);
      }
    }
  }

  private static void setAdditionalInfo(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    setFieldMappings(builder, ioJob);
    setOption(builder, ioJob);
    setSubProgresses(builder, ioJob);
    setErrors(builder, ioJob);
  }

  private static void setFieldMappings(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getFieldMappings() != null && !ioJob.getFieldMappings().isEmpty()) {
      try {
        String fieldMappingsJson = objectMapper.writeValueAsString(ioJob.getFieldMappings());
        builder.fieldMappings(fieldMappingsJson);
      }
      catch (Exception e) {
        throw new BssException("marshal field mappings failed", e);
      }
    }
  }

  private static void setOption(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getOption() != null) {
      try {
        String optionJson = objectMapper.writeValueAsString(ioJob.getOption());
        builder.option(optionJson);
      }
      catch (Exception e) {
        throw new BssException("marshal option failed", e);
      }
    }
  }

  private static void setSubProgresses(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getProgress() != null && ioJob.getProgress().getSubProgresses() != null && !ioJob.getProgress().getSubProgresses().isEmpty()) {
      try {
        String subProgressesJson = objectMapper.writeValueAsString(ioJob.getProgress().getSubProgresses());
        builder.subProgresses(subProgressesJson);
      }
      catch (Exception e) {
        throw new BssException("marshal sub progress failed", e);
      }
    }
  }

  private static void setErrors(DatasetIOJobEntity.DatasetIOJobEntityBuilder builder, IOJob ioJob) {
    if (ioJob.getErrors() != null && !ioJob.getErrors().isEmpty()) {
      try {
        String errorsJson = objectMapper.writeValueAsString(ioJob.getErrors());
        builder.errors(errorsJson);
      }
      catch (Exception e) {
        throw new BssException("marshal errors failed", e);
      }
    }
  }
}

