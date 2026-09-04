package com.iwhalecloud.bote.loop.data.application;

import com.iwhalecloud.bote.loop.client.data.dataset.DatasetSchemaService;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetSchemaRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetSchemaResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetSchemaRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetSchemaResponse;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldSchemaDTO;
import com.iwhalecloud.bote.loop.data.application.convertor.DatasetSchemaConvertor;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.DatasetDomainService;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 数据集模式管理服务实现类
 * 迁移对应关系: Go语言backend/modules/data/application/schema_app.go
 * - 功能: 数据集模式管理相关的应用层服务
 * - 主要方法:
 * * getDatasetSchema - 获取数据集当前的schema
 * * updateDatasetSchema - 覆盖更新schema
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetApplicationImpl结构体中的模式相关方法
 * - 使用Spring Service注解
 * - 依赖数据集API和转换器
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go指针操作 -> Java对象操作
 */
@Service
@RequiredArgsConstructor
public class DatasetSchemaServiceImpl implements DatasetSchemaService {
  private final IDatasetAPI datasetAPI;
  private final DatasetDomainService datasetDomainService;

  @Override
  public GetDatasetSchemaResponse getDatasetSchema(GetDatasetSchemaRequest request) {
    // 鉴权 - 对应Go代码第25-29行
    // TODO: 实现鉴权逻辑

    // 获取数据集信息 - 对应Go代码第30-36行
    Dataset dataset = datasetAPI.getDataset(request.getWorkspaceId(), request.getDatasetId());
    if (dataset == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " is not found");
    }

    Long schemaId = dataset.getSchemaId();

    // 获取模式信息 - 对应Go代码第39-42行
    DatasetSchema schema = datasetAPI.getSchema(request.getWorkspaceId(), schemaId);
    if (schema == null) {
      throw new BssException("schema=" + schemaId + " is not found");
    }

    // 处理字段过滤 - 对应Go代码第43-45行
    if (!request.getWithDeleted()) {
      schema.setFields(schema.getAvailableFields());
    }

    // 转换为DTO - 对应Go代码第46-50行
    List<FieldSchemaDTO> fieldDTOs = schema.getAvailableFields().stream()
      .map(DatasetSchemaConvertor::fieldSchemaDO2DTO)
      .collect(Collectors.toList());

    // 构建响应 - 对应Go代码第50行
    GetDatasetSchemaResponse response = new GetDatasetSchemaResponse();
    response.setFields(fieldDTOs);

    return response;
  }

  @Override
  public UpdateDatasetSchemaResponse updateDatasetSchema(UpdateDatasetSchemaRequest request) {
    // 鉴权 - 对应Go代码第54-58行
    // TODO: 实现鉴权逻辑

    // 转换字段模式 - 对应Go代码第59-62行
    List<FieldSchema> fields = request.getFields().stream()
      .map(DatasetSchemaConvertor::fieldSchemaDTO2DO)
      .collect(Collectors.toList());

    // 获取数据集信息 - 对应Go代码第63-66行
    Dataset dataset = datasetAPI.getDataset(request.getWorkspaceId(), request.getDatasetId());
    if (dataset == null) {
      throw new BssException("dataset=" + request.getDatasetId() + " is not found");
    }

    // 获取用户ID - 对应Go代码第68行
    String userId = SessionContext.getCurrentUserId();

    // 内容审核 - 对应Go代码第69-85行
    // TODO: 实现内容审核逻辑

    // TODO: 调用审核服务进行内容审核

    datasetDomainService.updateSchema(dataset, fields, userId);

    // 构建响应 - 对应Go代码第89行
    return new UpdateDatasetSchemaResponse();
  }
}
