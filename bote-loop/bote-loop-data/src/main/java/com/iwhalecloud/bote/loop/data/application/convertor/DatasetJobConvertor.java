package com.iwhalecloud.bote.loop.data.application.convertor;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.StorageProviderDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIODatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOEndpointDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOFileDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOJobDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOJobOptionDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOJobProgressDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.FieldMappingDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.FileFormatDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.JobStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.JobTypeDTO;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIODataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOEndpoint;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOFile;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOJobOption;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetIOJobProgress;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldMapping;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FileFormat;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobType;
import com.iwhalecloud.bote.loop.data.domain.entity.Provider;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据集作业转换器
 * 迁移对应关系: Go语言backend/modules/data/application/convertor/dataset/job.go
 * - 功能: 数据集作业相关的DO和DTO转换
 * - 主要方法:
 * * ioJobDO2DTO - IO作业DO转DTO
 * * datasetIOJobProgressDO2DTO - 数据集IO作业进度DO转DTO
 * * datasetIOJobOptionDO2DTO - 数据集IO作业选项DO转DTO
 * * fieldMappingDO2DTO - 字段映射DO转DTO
 * * datasetIOEndpointDO2DTO - 数据集IO端点DO转DTO
 * * datasetIOFileDO2DTO - 数据集IO文件DO转DTO
 * * datasetIODatasetDO2DTO - 数据集IO数据集DO转DTO
 * * ioJobDTO2DO - IO作业DTO转DO
 * * datasetIOJobProgressDTO2DO - 数据集IO作业进度DTO转DO
 * * datasetIOJobOptionDTO2DO - 数据集IO作业选项DTO转DO
 * * fieldMappingDTO2DO - 字段映射DTO转DO
 * * datasetIOEndpointDTO2DO - 数据集IO端点DTO转DO
 * * datasetIOFileDTO2DO - 数据集IO文件DTO转DO
 * * datasetIODatasetDTO2DO - 数据集IO数据集DTO转DO
 * <p>
 * Java实现说明:
 * - 对应Go的job.go文件
 * - 使用静态方法进行转换
 * - 处理嵌套对象转换
 * - 包含枚举类型转换
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go switch语句 -> Java switch表达式
 * - Go error返回 -> Java异常处理
 * - Go指针操作 -> Java对象操作
 */
public final class DatasetJobConvertor {

  private DatasetJobConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * IO作业DO转DTO
   * 迁移对应关系: Go语言IOJobDO2DTO方法
   *
   * @param job IO作业DO
   * @return IO作业DTO
   */
  public static DatasetIOJobDTO ioJobDO2DTO(IOJob job) {
    if (job == null) {
      return null;
    }

    List<FieldMappingDTO> fieldMappings = null;
    if (job.getFieldMappings() != null) {
      fieldMappings = job.getFieldMappings().stream()
        .map(DatasetJobConvertor::fieldMappingDO2DTO)
        .collect(Collectors.toList());
    }

    List<ItemErrorGroupDTO> errors = null;
    if (job.getErrors() != null) {
      errors = job.getErrors().stream()
        .map(DatasetItemConvertor::itemErrorGroupDO2DTO)
        .collect(Collectors.toList());
    }

    DatasetIOJobDTO dto = new DatasetIOJobDTO();
    dto.setId(job.getId());
    dto.setAppId(job.getAppId());
    dto.setSpaceId(job.getSpaceId());
    dto.setDatasetId(job.getDatasetId());
    dto.setJobType(convertJobTypeDO2DTO(job.getJobType()));
    dto.setSource(datasetIOEndpointDO2DTO(job.getSource()));
    dto.setTarget(datasetIOEndpointDO2DTO(job.getTarget()));
    dto.setFieldMappings(fieldMappings);
    dto.setOption(datasetIOJobOptionDO2DTO(job.getOption()));
    dto.setStatus(convertJobStatusDO2DTO(job.getStatus()));
    dto.setProgress(datasetIOJobProgressDO2DTO(job.getProgress()));
    dto.setErrors(errors);
    dto.setCreatedBy(job.getCreatedBy());
    dto.setCreatedAt(job.getCreatedAt());
    dto.setUpdatedBy(job.getUpdatedBy());
    dto.setUpdatedAt(job.getUpdatedAt());
    dto.setStartedAt(job.getStartedAt());
    dto.setEndedAt(job.getEndedAt());

    return dto;
  }

  /**
   * 数据集IO作业进度DO转DTO
   * 迁移对应关系: Go语言DatasetIOJobProgressDO2DTO方法
   *
   * @param progress 数据集IO作业进度DO
   * @return 数据集IO作业进度DTO
   */
  public static DatasetIOJobProgressDTO datasetIOJobProgressDO2DTO(DatasetIOJobProgress progress) {
    if (progress == null) {
      return null;
    }

    List<DatasetIOJobProgressDTO> subProgresses = null;
    if (progress.getSubProgresses() != null) {
      subProgresses = progress.getSubProgresses().stream()
        .map(DatasetJobConvertor::datasetIOJobProgressDO2DTO)
        .collect(Collectors.toList());
    }

    DatasetIOJobProgressDTO dto = new DatasetIOJobProgressDTO();
    dto.setTotal(progress.getTotal());
    dto.setProcessed(progress.getProcessed());
    dto.setAdded(progress.getAdded());
    dto.setName(progress.getName());
    dto.setSubProgresses(subProgresses);
    return dto;
  }

  /**
   * 数据集IO作业选项DO转DTO
   * 迁移对应关系: Go语言DatasetIOJobOptionDO2DTO方法
   *
   * @param option 数据集IO作业选项DO
   * @return 数据集IO作业选项DTO
   */
  public static DatasetIOJobOptionDTO datasetIOJobOptionDO2DTO(DatasetIOJobOption option) {
    if (option == null) {
      return null;
    }

    DatasetIOJobOptionDTO dto = new DatasetIOJobOptionDTO();
    dto.setOverwriteDataset(option.getOverwriteDataset());
    return dto;
  }

  /**
   * 字段映射DO转DTO
   * 迁移对应关系: Go语言FieldMappingDO2DTO方法
   *
   * @param mapping 字段映射DO
   * @return 字段映射DTO
   */
  public static FieldMappingDTO fieldMappingDO2DTO(FieldMapping mapping) {
    if (mapping == null) {
      return null;
    }

    FieldMappingDTO dto = new FieldMappingDTO();
    dto.setSource(mapping.getSource());
    dto.setTarget(mapping.getTarget());
    return dto;
  }

  /**
   * 数据集IO端点DO转DTO
   * 迁移对应关系: Go语言DatasetIOEndpointDO2DTO方法
   *
   * @param endpoint 数据集IO端点DO
   * @return 数据集IO端点DTO
   */
  public static DatasetIOEndpointDTO datasetIOEndpointDO2DTO(DatasetIOEndpoint endpoint) {
    if (endpoint == null) {
      return null;
    }

    DatasetIOEndpointDTO dto = new DatasetIOEndpointDTO();
    dto.setFile(datasetIOFileDO2DTO(endpoint.getFile()));
    dto.setDataset(datasetIODatasetDO2DTO(endpoint.getDataset()));
    return dto;
  }

  /**
   * 数据集IO文件DO转DTO
   * 迁移对应关系: Go语言DatasetIOFileDO2DTO方法
   *
   * @param file 数据集IO文件DO
   * @return 数据集IO文件DTO
   */
  public static DatasetIOFileDTO datasetIOFileDO2DTO(DatasetIOFile file) {
    if (file == null) {
      return null;
    }

    DatasetIOFileDTO dto = new DatasetIOFileDTO();
    dto.setProvider(convertStorageProviderDO2DTO(Provider.fromValue(file.getProvider())));
    dto.setPath(file.getPath());
    dto.setFormat(convertFileFormatDO2DTO(file.getFormat()));
    dto.setCompressFormat(convertFileFormatDO2DTO(file.getCompressFormat()));
    dto.setFiles(file.getFiles());
    return dto;
  }

  /**
   * 数据集IO数据集DO转DTO
   * 迁移对应关系: Go语言DatasetIODatasetDO2DTO方法
   *
   * @param dataset 数据集IO数据集DO
   * @return 数据集IO数据集DTO
   */
  public static DatasetIODatasetDTO datasetIODatasetDO2DTO(DatasetIODataset dataset) {
    if (dataset == null) {
      return null;
    }

    DatasetIODatasetDTO dto = new DatasetIODatasetDTO();
    dto.setSpaceId(dataset.getSpaceId());
    dto.setDatasetId(dataset.getDatasetId());
    dto.setVersionId(dataset.getVersionId());
    return dto;
  }

  /**
   * IO作业DTO转DO
   * 迁移对应关系: Go语言IOJobDTO2DO方法
   *
   * @param dto IO作业DTO
   * @return IO作业DO
   */
  public static IOJob ioJobDTO2DO(DatasetIOJobDTO dto) {
    if (dto == null) {
      return null;
    }

    List<FieldMapping> fieldMappings = null;
    if (dto.getFieldMappings() != null) {
      fieldMappings = dto.getFieldMappings().stream()
        .map(DatasetJobConvertor::fieldMappingDTO2DO)
        .collect(Collectors.toList());
    }

    List<ItemErrorGroup> errors = null;
    if (dto.getErrors() != null) {
      errors = dto.getErrors().stream()
        .map(DatasetItemConvertor::itemErrorGroupDTO2DO)
        .collect(Collectors.toList());
    }

    IOJob job = new IOJob();
    job.setId(dto.getId());
    job.setAppId(dto.getAppId());
    job.setSpaceId(dto.getSpaceId());
    job.setDatasetId(dto.getDatasetId());
    job.setJobType(convertJobTypeDTO2DO(dto.getJobType()));
    job.setSource(datasetIOEndpointDTO2DO(dto.getSource()));
    job.setTarget(datasetIOEndpointDTO2DO(dto.getTarget()));
    job.setFieldMappings(fieldMappings);
    job.setOption(datasetIOJobOptionDTO2DO(dto.getOption()));
    job.setStatus(convertJobStatusDTO2DO(dto.getStatus()));
    job.setProgress(datasetIOJobProgressDTO2DO(dto.getProgress()));
    job.setErrors(errors);
    job.setCreatedBy(dto.getCreatedBy());
    job.setCreatedAt(dto.getCreatedAt());
    job.setUpdatedBy(dto.getUpdatedBy());
    job.setUpdatedAt(dto.getUpdatedAt());
    job.setStartedAt(dto.getStartedAt());
    job.setEndedAt(dto.getEndedAt());

    return job;
  }

  /**
   * 数据集IO作业进度DTO转DO
   * 迁移对应关系: Go语言DatasetIOJobProgressDTO2DO方法
   *
   * @param dto 数据集IO作业进度DTO
   * @return 数据集IO作业进度DO
   */
  public static DatasetIOJobProgress datasetIOJobProgressDTO2DO(DatasetIOJobProgressDTO dto) {
    if (dto == null) {
      return null;
    }

    List<DatasetIOJobProgress> subProgresses = null;
    if (dto.getSubProgresses() != null) {
      subProgresses = dto.getSubProgresses().stream()
        .map(DatasetJobConvertor::datasetIOJobProgressDTO2DO)
        .collect(Collectors.toList());
    }

    DatasetIOJobProgress progress = new DatasetIOJobProgress();
    progress.setTotal(dto.getTotal());
    progress.setProcessed(dto.getProcessed());
    progress.setAdded(dto.getAdded());
    progress.setName(dto.getName());
    progress.setSubProgresses(subProgresses);
    return progress;
  }

  /**
   * 数据集IO作业选项DTO转DO
   * 迁移对应关系: Go语言DatasetIOJobOptionDTO2DO方法
   *
   * @param dto 数据集IO作业选项DTO
   * @return 数据集IO作业选项DO
   */
  public static DatasetIOJobOption datasetIOJobOptionDTO2DO(DatasetIOJobOptionDTO dto) {
    if (dto == null) {
      return null;
    }

    DatasetIOJobOption option = new DatasetIOJobOption();
    option.setOverwriteDataset(dto.getOverwriteDataset());
    return option;
  }

  /**
   * 字段映射DTO转DO
   * 迁移对应关系: Go语言FieldMappingDTO2DO方法
   *
   * @param dto 字段映射DTO
   * @return 字段映射DO
   */
  public static FieldMapping fieldMappingDTO2DO(FieldMappingDTO dto) {
    if (dto == null) {
      return null;
    }

    FieldMapping mapping = new FieldMapping();
    mapping.setSource(dto.getSource());
    mapping.setTarget(dto.getTarget());
    return mapping;
  }

  /**
   * 数据集IO端点DTO转DO
   * 迁移对应关系: Go语言DatasetIOEndpointDTO2DO方法
   *
   * @param dto 数据集IO端点DTO
   * @return 数据集IO端点DO
   */
  public static DatasetIOEndpoint datasetIOEndpointDTO2DO(DatasetIOEndpointDTO dto) {
    if (dto == null) {
      return null;
    }

    DatasetIOEndpoint endpoint = new DatasetIOEndpoint();
    endpoint.setFile(datasetIOFileDTO2DO(dto.getFile()));
    endpoint.setDataset(datasetIODatasetDTO2DO(dto.getDataset()));
    return endpoint;
  }

  /**
   * 数据集IO文件DTO转DO
   * 迁移对应关系: Go语言DatasetIOFileDTO2DO方法
   *
   * @param dto 数据集IO文件DTO
   * @return 数据集IO文件DO
   */
  public static DatasetIOFile datasetIOFileDTO2DO(DatasetIOFileDTO dto) {
    if (dto == null) {
      return null;
    }

    DatasetIOFile file = new DatasetIOFile();
    file.setProvider(convertStorageProviderDTO2DO(dto.getProvider()).getValue());
    file.setPath(dto.getPath());
    file.setFormat(convertFileFormatDTO2DO(dto.getFormat()));
    file.setCompressFormat(convertFileFormatDTO2DO(dto.getCompressFormat()));
    file.setFiles(dto.getFiles());
    return file;
  }

  /**
   * 数据集IO数据集DTO转DO
   * 迁移对应关系: Go语言DatasetIODatasetDTO2DO方法
   *
   * @param dto 数据集IO数据集DTO
   * @return 数据集IO数据集DO
   */
  public static DatasetIODataset datasetIODatasetDTO2DO(DatasetIODatasetDTO dto) {
    if (dto == null) {
      return null;
    }

    DatasetIODataset dataset = new DatasetIODataset();
    dataset.setSpaceId(dto.getSpaceId());
    dataset.setDatasetId(dto.getDatasetId());
    dataset.setVersionId(dto.getVersionId());
    return dataset;
  }

  /**
   * 作业类型DO转DTO
   * 迁移对应关系: Go语言JobType转换
   *
   * @param type DO层作业类型
   * @return DTO层作业类型
   */
  public static JobTypeDTO convertJobTypeDO2DTO(JobType type) {
    return JobTypeDTO.fromValue(type.getValue());
  }

  /**
   * 作业类型DTO转DO
   * 迁移对应关系: Go语言JobType转换
   *
   * @param type DTO层作业类型
   * @return DO层作业类型
   */
  public static JobType convertJobTypeDTO2DO(JobTypeDTO type) {
    return JobType.fromString(type.getName());
  }

  /**
   * 作业状态DO转DTO
   * 迁移对应关系: Go语言JobStatus转换
   *
   * @param status DO层作业状态
   * @return DTO层作业状态
   */
  public static JobStatusDTO convertJobStatusDO2DTO(JobStatus status) {
    if (status == null) {
      return null;
    }

    return switch (status) {
      case PENDING -> JobStatusDTO.PENDING;
      case RUNNING -> JobStatusDTO.RUNNING;
      case COMPLETED -> JobStatusDTO.COMPLETED;
      case FAILED -> JobStatusDTO.FAILED;
      case CANCELLED -> JobStatusDTO.CANCELLED;
      default -> null;
    };
  }

  /**
   * 作业状态DTO转DO
   * 迁移对应关系: Go语言JobStatus转换
   *
   * @param status DTO层作业状态
   * @return DO层作业状态
   */
  public static JobStatus convertJobStatusDTO2DO(JobStatusDTO status) {
    if (status == null) {
      return null;
    }

    return switch (status) {
      case PENDING -> JobStatus.PENDING;
      case RUNNING -> JobStatus.RUNNING;
      case COMPLETED -> JobStatus.COMPLETED;
      case FAILED -> JobStatus.FAILED;
      case CANCELLED -> JobStatus.CANCELLED;
      default -> null;
    };
  }

  /**
   * 文件格式DO转DTO
   * 迁移对应关系: Go语言FileFormat转换
   *
   * @param format DO层文件格式
   * @return DTO层文件格式
   */
  private static FileFormatDTO convertFileFormatDO2DTO(FileFormat format) {
    return FileFormatDTO.fromValue(format.getValue());
  }

  /**
   * 文件格式DTO转DO
   * 迁移对应关系: Go语言FileFormat转换
   *
   * @param format DTO层文件格式
   * @return DO层文件格式
   */
  private static FileFormat convertFileFormatDTO2DO(FileFormatDTO format) {
    return FileFormat.fromValue(format.getValue());
  }

  /**
   * 存储提供者DO转DTO
   * 迁移对应关系: Go语言StorageProvider转换
   *
   * @param provider DO层存储提供者
   * @return DTO层存储提供者
   */
  private static StorageProviderDTO convertStorageProviderDO2DTO(Provider provider) {
    if (provider == null) {
      return null;
    }

    return switch (provider) {
      case TOS -> StorageProviderDTO.TOS;
      case VETOS -> StorageProviderDTO.VETOS;
      case HDFS -> StorageProviderDTO.HDFS;
      case IMAGE_X -> StorageProviderDTO.IMAGEX;
      case S3 -> StorageProviderDTO.S3;
      case LOCAL_FS -> StorageProviderDTO.LOCALFS;
      case ABASE -> StorageProviderDTO.ABASE;
      case RDS -> StorageProviderDTO.RDS;
      default -> null;
    };
  }

  /**
   * 存储提供者DTO转DO
   * 迁移对应关系: Go语言StorageProvider转换
   *
   * @param provider DTO层存储提供者
   * @return DO层存储提供者
   */
  private static Provider convertStorageProviderDTO2DO(StorageProviderDTO provider) {
    if (provider == null) {
      return null;
    }

    return switch (provider) {
      case TOS -> Provider.TOS;
      case VETOS -> Provider.VETOS;
      case HDFS -> Provider.HDFS;
      case IMAGEX -> Provider.IMAGE_X;
      case S3 -> Provider.S3;
      case LOCALFS -> Provider.LOCAL_FS;
      case ABASE -> Provider.ABASE;
      case RDS -> Provider.RDS;
      default -> null;
    };
  }
}
