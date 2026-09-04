package com.iwhalecloud.bote.loop.data.application;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.data.dataset.DatasetManagementService;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.OrderByDTO;
import com.iwhalecloud.bote.loop.data.application.convertor.DatasetConvertor;
import com.iwhalecloud.bote.loop.data.application.convertor.DatasetSchemaConvertor;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.DatasetDomainService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.OrderBy;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.SearchDatasetsParam;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.UpdateDatasetParam;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据集管理服务实现类
 * 迁移对应关系: Go语言backend/modules/data/application/dataset_app.go
 * - 功能: 数据集管理相关的应用层服务
 * - 主要方法:
 * * createDataset - 创建数据集
 * * updateDataset - 更新数据集
 * * deleteDataset - 删除数据集
 * * listDatasets - 分页查询数据集列表
 * * getDataset - 获取数据集详情
 * * batchGetDatasets - 批量获取数据集
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetApplicationImpl结构体
 * - 使用Spring Service注解
 * - 依赖多个服务和仓库
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go指针操作 -> Java对象操作
 */
@Service
@RequiredArgsConstructor
public class DatasetManagementServiceImpl implements DatasetManagementService {
  private final DatasetDomainService datasetService;
  private final IDatasetAPI datasetAPI;

  @Override
  public CreateDatasetResponse createDataset(CreateDatasetRequest request) {
    // 获取用户ID和应用ID
    String userId = SessionContext.getCurrentUserId();
    Integer appId = SessionContext.getAppId();

    // 构建数据集实体 - 对应Go代码第85-99行
    Dataset dataset = new Dataset();
    dataset.setAppId(appId);
    dataset.setSpaceId(request.getWorkspaceId());
    dataset.setName(request.getName());
    dataset.setDescription(request.getDescription());
    dataset.setCategory(DatasetConvertor.convertCategoryDTO2DO(request.getCategory()));
    dataset.setBizCategory(request.getBizCategory());
    dataset.setSecurityLevel(DatasetConvertor.securityLevelDTO2DO(request.getSecurityLevel()));
    dataset.setVisibility(DatasetConvertor.visibilityDTO2DO(request.getVisibility()));
    dataset.setSpec(DatasetConvertor.specDTO2DO(request.getSpec()));
    dataset.setFeatures(DatasetConvertor.featuresDTO2DO(request.getFeatures()));
    dataset.setLastOperation(DatasetOpType.CREATE_DATASET);
    dataset.setCreatedBy(userId);
    dataset.setUpdatedBy(userId);
    dataset.setCatalogItemId(request.getCatalogItemId());

    // 转换字段模式 - 对应Go代码第100-107行
    List<FieldSchema> fields = new ArrayList<>();
    if (!CollectionUtils.isEmpty(request.getFields())) {
      fields = request.getFields().stream()
        .map(DatasetSchemaConvertor::fieldSchemaDTO2DO)
        .peek(field -> {
          field.setStatus(FieldStatus.AVAILABLE);
          field.setKey("");
        })
        .collect(Collectors.toList());
    }
    datasetService.createDataset(dataset, fields);
    CreateDatasetResponse response = new CreateDatasetResponse();
    response.setDatasetId(dataset.getId());
    return response;
  }

  @Override
  public UpdateDatasetResponse updateDataset(UpdateDatasetRequest request) {
    // 更新数据集 - 对应Go代码第154-163行
    UpdateDatasetParam param = new UpdateDatasetParam();
    param.setSpaceId(request.getWorkspaceId());
    param.setDatasetId(request.getDatasetId());
    param.setName(request.getName());
    param.setDescription(request.getDescription());
    param.setUpdatedBy(SessionContext.getCurrentUserId());
    param.setCatalogItemId(request.getCatalogItemId());

    datasetService.updateDataset(param);

    // 构建响应 - 对应Go代码第164行
    return new UpdateDatasetResponse();
  }

  @Override
  public DeleteDatasetResponse deleteDataset(DeleteDatasetRequest request) {
    // 删除数据集 - 对应Go代码第173-177行
    datasetService.deleteDataset(request.getWorkspaceId(), request.getDatasetId());

    // 构建响应 - 对应Go代码第177行
    return new DeleteDatasetResponse();
  }

  @Override
  public PageInfo<DatasetDTO> listDatasets(ListDatasetsRequest request) {
    // 构建排序条件 - 对应Go代码第191-197行
    OrderBy orderBy = null;
    if (!CollectionUtils.isEmpty(request.getOrderBys())) {
      OrderByDTO orderByDTO = request.getOrderBys().get(0);
      orderBy = new OrderBy();
      orderBy.setField(orderByDTO.getField());
      orderBy.setIsAsc(orderByDTO.getIsAsc());
    }

    // 搜索数据集 - 对应Go代码第198-212行
    SearchDatasetsParam param = new SearchDatasetsParam();
    param.setSpaceId(request.getWorkspaceId());
    param.setDatasetIds(request.getDatasetIds());
    param.setCategory(DatasetConvertor.convertCategoryDTO2DO(request.getCategory()));
    param.setName(request.getName());
    param.setCreatedBys(request.getCreatedBys());
    param.setPageNumber(request.getPageNumber());
    param.setPageSize(request.getPageSize());
    param.setOrderBy(orderBy);
    param.setBizCategorys(request.getBizCategorys());
    param.setCatalogItemId(request.getCatalogItemId());

    PageInfo<DatasetWithSchema> pageInfo = datasetService.searchDataset(param);
    List<DatasetWithSchema> datasetWithSchemas = pageInfo.getList();
    if (datasetWithSchemas == null) {
      return new PageInfo<>(Collections.emptyList());
    }

    // 处理字段过滤 - 对应Go代码第213-216行
    datasetWithSchemas = datasetWithSchemas.stream()
      .peek(ds -> {
        if (ds.getSchema() != null) {
          ds.getSchema().setFields(ds.getSchema().getAvailableFields());
        }
      })
      .toList();

    PageInfo<DatasetDTO> datasetDTOPageInfo = pageInfo.convert(ds -> DatasetConvertor.datasetDO2DTO(ds.getDataset(), ds.getSchema()));

    // 转换为DTO - 对应Go代码第217-222行
    List<DatasetDTO> dtos = datasetDTOPageInfo.getList();

    // 获取项目数量 - 对应Go代码第231-238行
    List<Long> datasetIds = datasetWithSchemas.stream()
      .map(o -> o.getDataset().getId())
      .collect(Collectors.toList());

    Map<Long, Long> itemCountsMap = datasetAPI.mGetItemCount(datasetIds);
    for (DatasetDTO dto : dtos) {
      dto.setItemCount(itemCountsMap.getOrDefault(dto.getId(), 0L));
    }

    return datasetDTOPageInfo;
  }

  @Override
  public GetDatasetResponse getDataset(GetDatasetRequest request) {
    // 鉴权 - 对应Go代码第243-247行
    // TODO: 实现鉴权逻辑

    // 获取数据集和模式 - 对应Go代码第248-251行
    DatasetWithSchema dsWithSchema = datasetService.getDatasetWithOpt(
      request.getWorkspaceId(),
      request.getDatasetId(),
      request.getWithDeleted()
    );

    if (dsWithSchema == null || dsWithSchema.getDataset() == null) {
      return new GetDatasetResponse();
    }

    // 处理字段过滤 - 对应Go代码第252-254行
    Boolean withDeleted = request.getWithDeleted();
    if ((withDeleted == null || !withDeleted) && dsWithSchema.getSchema() != null) {
      dsWithSchema.getSchema().setFields(dsWithSchema.getSchema().getAvailableFields());
    }

    // 转换为DTO - 对应Go代码第255-258行
    DatasetDTO dto = DatasetConvertor.datasetDO2DTO(dsWithSchema.getDataset(), dsWithSchema.getSchema());
    if (dto == null) {
      throw new BssException("dataset not found, datasetId=" + request.getDatasetId());
    }

    // 获取项目数量 - 对应Go代码第259-264行
    Long itemCount = datasetAPI.getItemCount(request.getDatasetId());
    dto.setItemCount(itemCount);

    // 构建响应 - 对应Go代码第265行
    GetDatasetResponse response = new GetDatasetResponse();
    response.setDataset(dto);

    return response;
  }

  @Override
  public BatchGetDatasetsResponse batchGetDatasets(BatchGetDatasetsRequest request) {
    // 鉴权 - 对应Go代码第270-277行
    // TODO: 实现鉴权逻辑

    // 批量获取数据集 - 对应Go代码第278-281行
    List<DatasetWithSchema> datasets = datasetService.batchGetDatasetWithOpt(
      request.getWorkspaceId(),
      request.getDatasetIds(),
      request.getWithDeleted()
    );

    // 转换为DTO - 对应Go代码第282-288行
    List<DatasetDTO> dtos = datasets.stream()
      .peek(ds -> ds.getSchema().setFields(ds.getSchema().getAvailableFields()))
      .map(ds -> DatasetConvertor.datasetDO2DTO(ds.getDataset(), ds.getSchema()))
      .collect(Collectors.toList());

    // 构建响应 - 对应Go代码第289行
    BatchGetDatasetsResponse response = new BatchGetDatasetsResponse();
    response.setDatasets(dtos);

    return response;
  }

}
