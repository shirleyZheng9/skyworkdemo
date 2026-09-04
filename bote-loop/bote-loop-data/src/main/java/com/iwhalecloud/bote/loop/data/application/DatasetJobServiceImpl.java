package com.iwhalecloud.bote.loop.data.application;

import com.iwhalecloud.bote.loop.client.data.dataset.DatasetJobService;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetIOJobRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetIOJobResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ImportDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ImportDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetIOJobsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetIOJobsResponse;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIODatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOEndpointDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOJobDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.FieldMappingDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.JobStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.JobTypeDTO;
import com.iwhalecloud.bote.loop.data.application.convertor.DatasetJobConvertor;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobType;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListIOJobsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.DatasetDomainService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 数据集任务管理服务实现类
 * 迁移对应关系: Go语言backend/modules/data/application/job_app.go
 * - 功能: 数据集任务管理相关的应用层服务
 * - 主要方法:
 * * importDataset - 导入数据
 * * getDatasetIOJob - 任务详情
 * * listDatasetIOJobs - 数据集任务列表
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetApplicationImpl结构体中的任务相关方法
 * - 使用Spring Service注解
 * - 依赖数据集API、领域服务和转换器
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go指针操作 -> Java对象操作
 */
@Service
@RequiredArgsConstructor
public class DatasetJobServiceImpl implements DatasetJobService {
  private final IDatasetAPI datasetAPI;
  private final DatasetDomainService datasetDomainService;

  @Override
  public ImportDatasetResponse importDataset(ImportDatasetRequest request) {
    // 鉴权 - 对应Go代码第27-31行
    // TODO: 实现鉴权逻辑

    // 检查导入数据集请求 - 对应Go代码第32-35行
    DatasetWithSchema datasetWithSchema = checkImportDatasetReq(request);
    if (datasetWithSchema == null) {
      throw new BssException("dataset validation failed");
    }

    // 构建任务 - 对应Go代码第37-38行
    DatasetIOJobDTO job = buildJob(request, datasetWithSchema);

    // 创建IO任务 - 对应Go代码第39-41行
    IOJob ioJob = DatasetJobConvertor.ioJobDTO2DO(job);
    datasetDomainService.createIOJob(ioJob);

    // 构建响应 - 对应Go代码第43行
    ImportDatasetResponse response = new ImportDatasetResponse();
    response.setJobId(ioJob.getId());

    return response;
  }

  @Override
  public GetDatasetIOJobResponse getDatasetIOJob(GetDatasetIOJobRequest request) {
    // 鉴权 - 对应Go代码第100-104行
    // TODO: 实现鉴权逻辑

    // 获取IO任务 - 对应Go代码第105-109行
    IOJob job = datasetDomainService.getIOJob(request.getJobId());
    if (job == null) {
      throw new BssException("job=" + request.getJobId() + " is not found");
    }

    // 转换为DTO - 对应Go代码第109行
    DatasetIOJobDTO jobDTO = DatasetJobConvertor.ioJobDO2DTO(job);

    // 构建响应
    GetDatasetIOJobResponse response = new GetDatasetIOJobResponse();
    response.setJob(jobDTO);

    return response;
  }

  @Override
  public ListDatasetIOJobsResponse listDatasetIOJobs(ListDatasetIOJobsRequest request) {
    // 鉴权 - 对应Go代码第113-121行
    // TODO: 实现鉴权逻辑

    // 构建查询参数 - 对应Go代码第122-127行
    ListIOJobsParams params = new ListIOJobsParams();
    params.setSpaceId(request.getWorkspaceId());
    params.setDatasetId(request.getDatasetId());

    if (!CollectionUtils.isEmpty(request.getTypes())) {
      List<JobType> types = request.getTypes().stream()
        .map(DatasetJobConvertor::convertJobTypeDTO2DO)
        .collect(Collectors.toList());
      params.setTypes(types);
    }

    if (!CollectionUtils.isEmpty(request.getStatuses())) {
      List<JobStatus> statuses = request.getStatuses().stream()
        .map(DatasetJobConvertor::convertJobStatusDTO2DO)
        .collect(Collectors.toList());
      params.setStatuses(statuses);
    }

    // 查询任务列表 - 对应Go代码第122-130行
    List<IOJob> jobs = datasetAPI.listIOJobs(params);

    // 转换为DTO - 对应Go代码第131行
    List<DatasetIOJobDTO> jobDTOs = jobs.stream()
      .map(DatasetJobConvertor::ioJobDO2DTO)
      .collect(Collectors.toList());

    // 构建响应
    ListDatasetIOJobsResponse response = new ListDatasetIOJobsResponse();
    response.setJobs(jobDTOs);

    return response;
  }

  /**
   * 检查导入数据集请求
   * 迁移对应关系: Go语言checkImportDatasetReq方法
   *
   * @param request 导入数据集请求
   * @return 数据集和模式信息
   */
  private DatasetWithSchema checkImportDatasetReq(ImportDatasetRequest request) {
    // 获取数据集信息 - 对应Go代码第47-50行
    DatasetWithSchema datasetWithSchema = datasetDomainService.getDataset(
      request.getWorkspaceId(),
      request.getDatasetId()
    );
    if (datasetWithSchema == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " is not found");
    }

    // TODO: 检查数据集状态 - 对应Go代码第51行

    // 检查字段映射 - 对应Go代码第53-57行
    List<String> allFields = datasetWithSchema.getSchema().getAvailableFields().stream()
      .map(FieldSchema::getName)
      .toList();

    List<String> mappingFields = request.getFieldMappings().stream()
      .map(FieldMappingDTO::getTarget)
      .toList();

    List<String> diff = mappingFields.stream()
      .filter(field -> !allFields.contains(field))
      .collect(Collectors.toList());

    if (!diff.isEmpty()) {
      throw new BssException("target field " + String.join(", ", diff) + " not found in dataset");
    }

    // 检查文件 - 对应Go代码第59-77行
    // TODO: 实现文件状态检查
    // File file = new File(request.getFile().getPath());
    // if (!file.exists()) {
    //     throw new BssException("file not found: " + request.getFile().getPath());
    // }
    // if (file.isDirectory()) {
    //     throw new BssException("file is a directory");
    // }

    // 检查文件格式 - 对应Go代码第68-75行
    String filePath = request.getFile().getPath();
    String ext = getFileExtension(filePath).toLowerCase();
    String format = request.getFile().getFormat().toString().toLowerCase();

    if (request.getFile().getCompressFormat() != null) {
      format = request.getFile().getCompressFormat().toString().toLowerCase();
    }

    if (!("." + format).equals(ext)) {
      throw new BssException("file format mismatch, want " + format + ", got " + ext);
    }

    return datasetWithSchema;
  }

  /**
   * 构建任务
   * 迁移对应关系: Go语言buildJob方法
   *
   * @param request 导入数据集请求
   * @param datasetWithSchema 数据集和模式信息
   * @return IO任务DTO
   */
  private DatasetIOJobDTO buildJob(ImportDatasetRequest request, DatasetWithSchema datasetWithSchema) {
    // 获取用户ID - 对应Go代码第81行
    String userId = SessionContext.getCurrentUserId();

    // 构建任务DTO - 对应Go代码第82-96行

    DatasetIOEndpointDTO source = new DatasetIOEndpointDTO();
    source.setFile(request.getFile());

    DatasetIOEndpointDTO target = new DatasetIOEndpointDTO();
    DatasetIODatasetDTO dataset = new DatasetIODatasetDTO();
    dataset.setDatasetId(datasetWithSchema.getDataset().getId());
    target.setDataset(dataset);

    DatasetIOJobDTO job = new DatasetIOJobDTO();
    job.setAppId(datasetWithSchema.getDataset().getAppId());
    job.setSpaceId(datasetWithSchema.getDataset().getSpaceId());
    job.setDatasetId(datasetWithSchema.getDataset().getId());
    job.setJobType(JobTypeDTO.IMPORT_FROM_FILE);
    job.setSource(source);
    job.setTarget(target);
    job.setFieldMappings(request.getFieldMappings());
    job.setOption(request.getOption());
    job.setStatus(JobStatusDTO.PENDING);
    job.setCreatedBy(userId);
    job.setUpdatedBy(userId);

    return job;
  }

  /**
   * 获取文件扩展名
   *
   * @param filePath 文件路径
   * @return 文件扩展名
   */
  private String getFileExtension(String filePath) {
    int lastDotIndex = filePath.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return "";
    }
    return filePath.substring(lastDotIndex);
  }
}
