package com.iwhalecloud.bote.loop.data.application;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.data.dataset.DatasetVersionService;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetVersionsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetVersionsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetVersionResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetVersionResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetVersionsRequest;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVersionDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.OrderByDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.VersionedDatasetDTO;
import com.iwhalecloud.bote.loop.data.application.convertor.DatasetConvertor;
import com.iwhalecloud.bote.loop.data.application.convertor.DatasetVersionConvertor;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SnapshotStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetVersionsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.DatasetDomainService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.OrderBy;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.VersionWithDatasetResult;
import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 数据集版本管理服务实现类
 * 迁移对应关系: Go语言backend/modules/data/application/version_app.go
 * - 功能: 数据集版本管理相关的应用层服务
 * - 主要方法:
 * * createDatasetVersion - 生成一个新版本
 * * listDatasetVersions - 版本列表
 * * getDatasetVersion - 获取指定版本的数据集详情
 * * batchGetDatasetVersions - 批量获取指定版本的数据集详情
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetApplicationImpl结构体中的版本相关方法
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
public class DatasetVersionServiceImpl implements DatasetVersionService {
  private final IDatasetAPI datasetAPI;
  private final DatasetDomainService datasetDomainService;

  @Override
  public CreateDatasetVersionResponse createDatasetVersion(CreateDatasetVersionRequest request) {
    // 鉴权 - 对应Go代码第29-33行
    // TODO: 实现鉴权逻辑

    // 获取数据集信息 - 对应Go代码第34-38行
    DatasetWithSchema datasetWithSchema = datasetDomainService.getDataset(request.getWorkspaceId(), request.getDatasetId());
    Dataset dataset = datasetWithSchema.getDataset();
    if (dataset == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " is not found");
    }
    dataset.setUpdatedBy(SessionContext.getCurrentUserId());

    // 构建版本实体 - 对应Go代码第39-47行
    DatasetVersion version = new DatasetVersion();
    version.setAppId(SessionContext.getAppId());
    version.setSpaceId(request.getWorkspaceId());
    version.setDatasetId(request.getDatasetId());
    version.setVersion(request.getVersion());
    version.setDescription(request.getDesc());
    version.setSnapshotStatus(SnapshotStatus.UNSTARTED);
    version.setItemCount(0L); // 初始项目数量为0
    version.setUpdateVersion(1L); // 初始更新版本号为1
    version.setCreatedBy(SessionContext.getCurrentUserId());
    version.setCreatedAt(new Date()); // 设置创建时间
    // 创建版本 - 对应Go代码第63-65行
    datasetDomainService.createVersion(datasetWithSchema, version);

    // 构建响应 - 对应Go代码第67行
    CreateDatasetVersionResponse response = new CreateDatasetVersionResponse();
    response.setId(version.getId());

    return response;
  }

  @Override
  public PageInfo<DatasetVersionDTO> listDatasetVersions(ListDatasetVersionsRequest request) {
    // 鉴权 - 对应Go代码第71-75行
    // TODO: 实现鉴权逻辑

    // 构建排序条件 - 对应Go代码第76-82行
    OrderBy orderBy = null;
    if (!CollectionUtils.isEmpty(request.getOrderBys())) {
      OrderByDTO orderByDTO = request.getOrderBys().get(0);
      orderBy = new OrderBy();
      orderBy.setField(orderByDTO.getField());
      orderBy.setIsAsc(orderByDTO.getIsAsc());
    }

    // 构建分页器 - 对应Go代码第84-88行
    Paginator paginator = new Paginator();
    paginator.setIdColumn(orderBy != null ? orderBy.getField() : "created_at");
    paginator.setAsc(orderBy != null ? orderBy.getIsAsc() : false);
    paginator.setLimit(request.getPageSize() != null ? request.getPageSize() : 20);
    paginator.setOffset(request.getPageNumber() != null && request.getPageNumber() > 0 ? (request.getPageNumber() - 1) * paginator.getLimit() : 0);

    // 构建查询参数 - 对应Go代码第90-95行
    ListDatasetVersionsParams param = new ListDatasetVersionsParams();
    param.setSpaceId(request.getWorkspaceId());
    param.setDatasetId(request.getDatasetId());
    param.setVersionLike(request.getVersionLike());
    param.setPaginator(paginator);

    // 查询版本列表 - 对应Go代码第97-100行
    PageInfo<DatasetVersion> versions = datasetAPI.listVersions(param);

    return versions.convert(DatasetVersionConvertor::versionDO2DTO);
  }

  @Override
  public GetDatasetVersionResponse getDatasetVersion(GetDatasetVersionRequest request) {
    // 鉴权 - 对应Go代码第117-121行
    // TODO: 实现鉴权逻辑

    // 获取版本和数据集信息 - 对应Go代码第122-125行
    VersionWithDatasetResult versionWithDatasetResult = datasetDomainService.getVersionWithOpt(
      request.getWorkspaceId(),
      request.getVersionId(),
      request.getWithDeleted()
    );

    final DatasetWithSchema ds = versionWithDatasetResult.getDataset();

    if (ds == null) {
      return new GetDatasetVersionResponse();
    }

    // 处理字段过滤 - 对应Go代码第126行
    ds.getSchema().setFields(ds.getSchema().getAvailableFields());

    // 转换为DTO - 对应Go代码第127-134行
    DatasetDTO datasetDTO = DatasetConvertor.datasetDO2DTO(
      ds.getDataset(),
      ds.getSchema()
    );

    DatasetVersionDTO versionDTO = DatasetVersionConvertor.versionDO2DTO(versionWithDatasetResult.getVersion());

    // 构建响应 - 对应Go代码第135-138行
    GetDatasetVersionResponse response = new GetDatasetVersionResponse();
    response.setVersion(versionDTO);
    response.setDataset(datasetDTO);

    return response;
  }

  @Override
  public BatchGetDatasetVersionsResponse batchGetDatasetVersions(BatchGetDatasetVersionsRequest request) {
    // 空值检查 - 对应Go代码第142-144行
    if (CollectionUtils.isEmpty(request.getVersionIds())) {
      return new BatchGetDatasetVersionsResponse();
    }

    // 鉴权 - 对应Go代码第145-153行
    // TODO: 实现鉴权逻辑

    // 批量获取版本化数据集 - 对应Go代码第154-157行
    // 处理withDeleted可能为null的情况，默认为false
    boolean withDeleted = request.getWithDeleted() != null && request.getWithDeleted();
    List<VersionWithDatasetResult> versionedDatasets = datasetDomainService.batchGetVersionedDatasetsWithOpt(
      request.getWorkspaceId(),
      request.getVersionIds(),
      withDeleted
    );

    // 转换为DTO - 对应Go代码第158-174行
    // 处理withDeleted可能为null的情况，默认为false
    final boolean finalWithDeleted = withDeleted; // 用于lambda表达式
    List<VersionedDatasetDTO> versionedDatasetDTOs = versionedDatasets.stream()
      .peek(d -> {
        if (!finalWithDeleted) {
          d.getDataset().getSchema().setFields(d.getDataset().getSchema().getAvailableFields());
        }
      })
      .map(d -> {
        DatasetDTO datasetDTO = DatasetConvertor.datasetDO2DTO(d.getDataset().getDataset(), d.getDataset().getSchema());
        DatasetVersionDTO versionDTO = DatasetVersionConvertor.versionDO2DTO(d.getVersion());

        VersionedDatasetDTO versionedDatasetDTO = new VersionedDatasetDTO();
        versionedDatasetDTO.setDataset(datasetDTO);
        versionedDatasetDTO.setVersion(versionDTO);
        return versionedDatasetDTO;
      })
      .collect(Collectors.toList());

    // 构建响应 - 对应Go代码第175行
    BatchGetDatasetVersionsResponse response = new BatchGetDatasetVersionsResponse();
    response.setVersionedDataset(versionedDatasetDTOs);

    return response;
  }
}
