package com.iwhalecloud.bote.loop.evaluation.application;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.iwhalecloud.bote.common.util.ExcelUtil;
import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoService;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.FieldMappingDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetItemDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetSchemaDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.EvaluationSetApplicationService;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchCreateEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchCreateEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchDeleteEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchDeleteEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ClearEvaluationSetDraftItemRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ClearEvaluationSetDraftItemResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.DeleteEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.DeleteEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ImportEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ImportEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetItemRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetItemResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetSchemaRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetSchemaResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.VersionedEvaluationSetDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset.EvaluationSetApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset.EvaluationSetItemApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset.EvaluationSetSchemaApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset.EvaluationSetVersionApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchCreateEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchCreateEvaluationSetItemsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvaluationSetVersionsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BizCategory;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateEvaluationSetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateEvaluationSetVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetVersionsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UpdateEvaluationSetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetItemService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetSchemaService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetVersionService;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationSetApplicationServiceImpl implements EvaluationSetApplicationService {

  private static final Logger logger = LoggerFactory.getLogger(EvaluationSetApplicationServiceImpl.class);

  private final EvaluationSetService evaluationSetService;
  private final EvaluationSetSchemaService evaluationSetSchemaService;
  private final EvaluationSetVersionService evaluationSetVersionService;
  private final EvaluationSetItemService evaluationSetItemService;
  private final UserInfoService userInfoService;

  @Override
  public CreateEvaluationSetResponse createEvaluationSet(CreateEvaluationSetRequest request) {
    if (request.getName() == null) {
      throw new BssException("name is nil");
    }
    if (request.getEvaluationSetSchema() == null) {
      throw new BssException("schema is nil");
    }
    // domain调用
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .appId(SessionContext.getAppId())
      .build();

    CreateEvaluationSetParam param = CreateEvaluationSetParam.builder()
      .spaceId(request.getWorkspaceId())
      .name(request.getName())
      .description(request.getDescription())
      .evaluationSetSchema(EvaluationSetSchemaApplicationConvertor.convertSchemaDTO2DO(request.getEvaluationSetSchema()))
      .bizCategory(BizCategory.fromString(request.getBizCategory().getValue()))
      .session(session)
      .catalogItemId(request.getCatalogItemId())
      .build();
    Long id = evaluationSetService.createEvaluationSet(param);

    // 返回结果构建
    return CreateEvaluationSetResponse.builder().evaluationSetId(id).build();
  }

  @Override
  public UpdateEvaluationSetResponse updateEvaluationSet(UpdateEvaluationSetRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // TODO 鉴权
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), null);
    if (set == null) {
      throw new BssException("errno set not found");
    }

    // domain调用
    UpdateEvaluationSetParam param = UpdateEvaluationSetParam.builder()
      .spaceId(request.getWorkspaceId())
      .evaluationSetId(request.getEvaluationSetId())
      .name(request.getName())
      .description(request.getDescription())
      .catalogItemId(request.getCatalogItemId())
      .build();
    evaluationSetService.updateEvaluationSet(param);

    // 返回结果构建
    return UpdateEvaluationSetResponse.builder().build();
  }

  @Override
  public DeleteEvaluationSetResponse deleteEvaluationSet(DeleteEvaluationSetRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // TODO 鉴权
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), null);
    if (set == null) {
      throw new BssException("errno set not found");
    }

    // domain调用
    evaluationSetService.deleteEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId());

    // 返回结果构建
    return DeleteEvaluationSetResponse.builder().build();
  }

  @Override
  public GetEvaluationSetResponse getEvaluationSet(GetEvaluationSetRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), request.getDeletedAt());
    if (set == null) {
      throw new BssException("experiment set not found");
    }

    // 返回结果构建
    EvaluationSetDTO dto = EvaluationSetApplicationConvertor.convertDO2DTO(set);
    userInfoService.packUserInfoObj(dto);
    EvaluationSetVersionDTO evaluationSetVersion = dto.getEvaluationSetVersion();
    userInfoService.packUserInfoObj(evaluationSetVersion);
    if (evaluationSetVersion != null) {
      userInfoService.packUserInfoObj(evaluationSetVersion.getEvaluationSetSchema());
    }
    return GetEvaluationSetResponse.builder()
      .evaluationSet(dto)
      .build();
  }

  @Override
  public PageInfo<EvaluationSetDTO> listEvaluationSets(ListEvaluationSetsRequest request) {
    // 参数校验
    Assert.notNull(request, "req is nil");

    // domain调用
    ListEvaluationSetsParam param = ListEvaluationSetsParam.builder()
      .spaceId(request.getWorkspaceId())
      .evaluationSetIds(request.getEvaluationSetIds())
      .catalogItemId(request.getCatalogItemId())
      .name(request.getName())
      .creators(request.getCreators())
      .pageNumber(request.getPageNumber())
      .pageSize(request.getPageSize())
      .pageToken(request.getPageToken())
      .orderBys(request.getOrderBys() != null ? request.getOrderBys().stream()
        .map(o -> OrderBy.builder()
          .field(o.getField())
          .isAsc(o.getIsAsc())
          .build()
        ).collect(Collectors.toList()) : null)
      .build();

    PageInfo<EvaluationSet> result = evaluationSetService.listEvaluationSets(param);
    PageInfo<EvaluationSetDTO> pageInfo = result.convert(EvaluationSetApplicationConvertor::convertDO2DTO);
    List<EvaluationSetDTO> evaluationSetDTOS = pageInfo.getList();
    List<EvaluationSetVersionDTO> versionDTOS = evaluationSetDTOS.stream().map(EvaluationSetDTO::getEvaluationSetVersion).toList();
    List<EvaluationSetSchemaDTO> schemaDTOList = versionDTOS.stream().filter(Objects::nonNull).map(EvaluationSetVersionDTO::getEvaluationSetSchema).toList();
    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    userInfoCarriers.addAll(evaluationSetDTOS);
    userInfoCarriers.addAll(versionDTOS);
    userInfoCarriers.addAll(schemaDTOList);
    userInfoService.packUserInfoList(userInfoCarriers);
    // 返回结果构建
    return pageInfo;
  }

  @Override
  public CreateEvaluationSetVersionResponse createEvaluationSetVersion(CreateEvaluationSetVersionRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getVersion() == null) {
      throw new BssException("version is nil");
    }

    // TODO 鉴权
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), null);
    if (set == null) {
      throw new BssException("errno set not found");
    }

    // domain调用
    CreateEvaluationSetVersionParam param = CreateEvaluationSetVersionParam.builder()
      .spaceId(request.getWorkspaceId())
      .evaluationSetId(request.getEvaluationSetId())
      .version(request.getVersion())
      .description(request.getDesc())
      .build();
    Long id = evaluationSetVersionService.createEvaluationSetVersion(param);

    // 返回结果构建
    return CreateEvaluationSetVersionResponse.builder()
      .id(id)
      .build();
  }

  @Override
  public GetEvaluationSetVersionResponse getEvaluationSetVersion(GetEvaluationSetVersionRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // TODO 鉴权
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), request.getDeletedAt());
    if (set == null) {
      throw new BssException("errno set not found");
    }

    // domain调用
    EvaluationSetVersionResult result = evaluationSetVersionService.getEvaluationSetVersion(request.getWorkspaceId(), request.getVersionId(), request.getDeletedAt());

    // 返回结果构建
    EvaluationSetDTO evaluationSetDTO = EvaluationSetApplicationConvertor.convertDO2DTO(result.getEvaluationSet());
    EvaluationSetVersionDTO versionDTO = EvaluationSetVersionApplicationConvertor.convertVersionDO2DTO(result.getVersion());
    userInfoService.packUserInfoObj(evaluationSetDTO);
    userInfoService.packUserInfoObj(versionDTO);

    return GetEvaluationSetVersionResponse.builder()
      .version(versionDTO)
      .evaluationSet(evaluationSetDTO)
      .build();
  }

  @Override
  public PageInfo<EvaluationSetVersionDTO> listEvaluationSetVersions(ListEvaluationSetVersionsRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // TODO 鉴权
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), null);
    if (set == null) {
      throw new BssException("errno set not found");
    }

    // domain调用
    ListEvaluationSetVersionsParam param = ListEvaluationSetVersionsParam.builder()
      .spaceId(request.getWorkspaceId())
      .evaluationSetId(request.getEvaluationSetId())
      .pageSize(request.getPageSize())
      .pageNumber(request.getPageNumber())
      .pageToken(request.getPageToken())
      .versionLike(request.getVersionLike())
      .build();

    PageInfo<EvaluationSetVersion> result = evaluationSetVersionService.listEvaluationSetVersions(param);
    PageInfo<EvaluationSetVersionDTO> pageInfo = result.convert(EvaluationSetVersionApplicationConvertor::convertVersionDO2DTO);
    List<EvaluationSetVersionDTO> evaluationSetVersionDTOS = pageInfo.getList();
    userInfoService.packUserInfoList(evaluationSetVersionDTOS);
    // 返回结果构建
    return pageInfo;
  }

  @Override
  public BatchGetEvaluationSetVersionsResponse batchGetEvaluationSetVersions(BatchGetEvaluationSetVersionsRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // TODO 鉴权

    // domain调用
    List<BatchGetEvaluationSetVersionsResult> sets = evaluationSetVersionService.batchGetEvaluationSetVersions(
      request.getWorkspaceId(), request.getVersionIds(), request.getDeletedAt());

    List<VersionedEvaluationSetDTO> res = sets.stream()
      .map(o -> VersionedEvaluationSetDTO.builder()
        .evaluationSet(EvaluationSetApplicationConvertor.convertDO2DTO(o.getEvaluationSet()))
        .version(EvaluationSetVersionApplicationConvertor.convertVersionDO2DTO(o.getVersion()))
        .build())
      .toList();

    List<EvaluationSetDTO> evaluationSetDTOS = res.stream().map(VersionedEvaluationSetDTO::getEvaluationSet).collect(Collectors.toList());
    userInfoService.packUserInfoList(evaluationSetDTOS);
    // 返回结果构建
    return BatchGetEvaluationSetVersionsResponse.builder()
      .versionedEvaluationSets(res)
      .build();
  }

  @Override
  public UpdateEvaluationSetSchemaResponse updateEvaluationSetSchema(UpdateEvaluationSetSchemaRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // TODO 鉴权
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), null);
    if (set == null) {
      throw new BssException("errno set not found");
    }

    // domain调用
    evaluationSetSchemaService.updateEvaluationSetSchema(
      request.getWorkspaceId(),
      request.getEvaluationSetId(),
      EvaluationSetSchemaApplicationConvertor.convertFieldSchemaDTO2DOs(request.getFields())
    );

    // 返回结果构建
    return UpdateEvaluationSetSchemaResponse.builder().build();
  }

  @Override
  public BatchCreateEvaluationSetItemsResponse batchCreateEvaluationSetItems(BatchCreateEvaluationSetItemsRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new BssException("items is nil");
    }

    // TODO 鉴权
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), null);
    if (set == null) {
      throw new BssException("errno set not found");
    }

    // domain调用
    BatchCreateEvaluationSetItemsParam param = BatchCreateEvaluationSetItemsParam.builder()
      .spaceId(request.getWorkspaceId())
      .evaluationSetId(request.getEvaluationSetId())
      .items(EvaluationSetItemApplicationConvertor.convertItemDTO2DOs(request.getItems()))
      .skipInvalidItems(request.getSkipInvalidItems())
      .allowPartialAdd(request.getAllowPartialAdd())
      .build();

    BatchCreateEvaluationSetItemsResult result = evaluationSetItemService.batchCreateEvaluationSetItems(param);

    // 返回结果构建
    return BatchCreateEvaluationSetItemsResponse.builder()
      .addedItems(result.getIdMap())
      .errors(EvaluationSetItemApplicationConvertor.convertItemErrorGroupDO2DTOs(result.getErrors()))
      .build();
  }

  @Override
  public UpdateEvaluationSetItemResponse updateEvaluationSetItem(UpdateEvaluationSetItemRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // TODO 鉴权
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), null);
    if (set == null) {
      throw new BssException("errno set not found");
    }

    // domain调用
    evaluationSetItemService.updateEvaluationSetItem(
      request.getWorkspaceId(),
      request.getEvaluationSetId(),
      request.getItemId(),
      EvaluationSetItemApplicationConvertor.convertTurnDTO2DOs(request.getTurns())
    );

    // 返回结果构建
    return UpdateEvaluationSetItemResponse.builder().build();
  }

  @Override
  public BatchDeleteEvaluationSetItemsResponse batchDeleteEvaluationSetItems(BatchDeleteEvaluationSetItemsRequest request) {
    // 参数校验
    Assert.notNull(request, "req is nil");
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), null);
    Assert.notNull(set, "errno set not found");
    // domain调用
    evaluationSetItemService.batchDeleteEvaluationSetItems(
      request.getWorkspaceId(),
      request.getEvaluationSetId(),
      request.getItemIds()
    );
    // 返回结果构建
    return BatchDeleteEvaluationSetItemsResponse.builder().build();
  }

  @Override
  public PageInfo<EvaluationSetItemDTO> listEvaluationSetItems(ListEvaluationSetItemsRequest request) {
    Assert.notNull(request, "req is nil");

    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), true);
    Assert.notNull(set, "评测集未找到");

    // domain调用
    ListEvaluationSetItemsParam param = ListEvaluationSetItemsParam.builder()
      .spaceId(request.getWorkspaceId())
      .evaluationSetId(request.getEvaluationSetId())
      .versionId(request.getVersionId())
      .pageNumber(request.getPageNumber())
      .pageSize(request.getPageSize())
      .orderBys(request.getOrderBys() != null ? request.getOrderBys().stream().map(o -> OrderBy.builder().field(o.getField()).isAsc(o.getIsAsc()).build()).toList() : null)
      .build();

    PageInfo<EvaluationSetItem> result = evaluationSetItemService.listEvaluationSetItems(param);
    PageInfo<EvaluationSetItemDTO> resultDto = result.convert(EvaluationSetItemApplicationConvertor::convertItemDO2DTO);
    List<EvaluationSetItemDTO> evaluationSetItemDTOS = resultDto.getList();
    userInfoService.packUserInfoList(evaluationSetItemDTOS);
    return resultDto;
  }

  @Override
  public BatchGetEvaluationSetItemsResponse batchGetEvaluationSetItems(BatchGetEvaluationSetItemsRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // TODO 鉴权
    // 先获取评估集信息进行鉴权
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getWorkspaceId(), request.getEvaluationSetId(), true);
    if (set == null) {
      throw new BssException("errno set not found");
    }

    // domain调用
    BatchGetEvaluationSetItemsParam param = BatchGetEvaluationSetItemsParam.builder()
      .spaceId(request.getWorkspaceId())
      .evaluationSetId(request.getEvaluationSetId())
      .versionId(request.getVersionId())
      .itemIds(request.getItemIds())
      .build();

    PageInfo<EvaluationSetItem> items = evaluationSetItemService.batchGetEvaluationSetItems(param);

    List<EvaluationSetItemDTO> evaluationSetItemDTOS = EvaluationSetItemApplicationConvertor.convertItemDO2DTOs(items.getList());
    userInfoService.packUserInfoList(evaluationSetItemDTOS);
    // 返回结果构建
    return BatchGetEvaluationSetItemsResponse.builder()
      .items(evaluationSetItemDTOS)
      .build();
  }

  @Override
  public ClearEvaluationSetDraftItemResponse clearEvaluationSetDraftItem(ClearEvaluationSetDraftItemRequest request) {
    // domain调用
    evaluationSetItemService.clearEvaluationSetDraftItem(request.getWorkspaceId(), request.getEvaluationSetId());
    // 返回结果构建
    return ClearEvaluationSetDraftItemResponse.builder().build();
  }

  public ImportEvaluationSetItemsResponse batchImportEvaluationSetItems(MultipartFile file, ImportEvaluationSetItemsRequest request) {
    try {
      // 1. 解析Excel文件
      List<Map<String, Object>> dataList = parseExcel(file, request);
      String errMsg = (String) dataList.get(0).get("errMsg");
      if (!StringUtils.isEmpty(errMsg)) {
        throw new BssException(errMsg);
      }
      return handlerData(dataList, request);
    }
    catch (IOException e) {
      logger.error("Excel导入失败", e);
      throw new BssException("批量导入评测集数据失败: " + e.getMessage(), e);
    }
  }

  private List<Map<String, Object>> parseExcel(MultipartFile file, ImportEvaluationSetItemsRequest request) throws IOException {
    // 使用现有的ExcelUtil类解析Excel文件
    boolean isXls = !Strings.CI.endsWith(file.getOriginalFilename(), ".xlsx");
    try (InputStream inputStream = file.getInputStream()) {
      List<List<String>> excelDataList = ExcelUtil.getExcelData(inputStream, 0, isXls);
      List<Map<String, Object>> results = Lists.newArrayList();
      if (CollectionUtils.isEmpty(excelDataList)) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("errMsg", "工作表为空，请检查文件内容!");
        results.add(result);
        return results;
      }
      List<String> firstRow = excelDataList.get(0);
      List<FieldMappingDTO> fieldMappings = request.getFieldMappings();
      if (fieldMappings.stream().anyMatch(o -> !firstRow.contains(o.getTarget()))) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("errMsg", "模板不匹配，请使用正确模板!");
        results.add(result);
        return results;
      }
      for (int i = 1; i < excelDataList.size(); i++) {
        Map<String, Object> result = Maps.newHashMap();
        List<String> rowData = excelDataList.get(i);
        if (rowData.stream().anyMatch(StringUtils::isBlank)) {
          continue;
        }
        for (int j = 0; j < firstRow.size() && j < rowData.size(); j++) {
          String title = firstRow.get(j);
          String targetValue = rowData.get(j);
          result.put(title, targetValue);
        }
        results.add(result);
      }
      if (CollectionUtils.isEmpty(results)) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("errMsg", "工作表为空，请检查文件内容!");
        results.add(result);
        return results;
      }
      return results;
    }
  }

  private ImportEvaluationSetItemsResponse handlerData(List<Map<String, Object>> dataList, ImportEvaluationSetItemsRequest request) {
    ImportEvaluationSetItemsResponse resp = new ImportEvaluationSetItemsResponse();
    List<FieldMappingDTO> fieldMappings = request.getFieldMappings();
    EvaluationSet set = evaluationSetService.getEvaluationSet(request.getSpaceId(), request.getEvaluationSetId(), null);
    Assert.notNull(set, "评测集未找到");
    List<EvaluationSetItem> items = Lists.newArrayList();
    for (int i = 0; i < dataList.size(); i++) {
      Map<String, Object> importData = dataList.get(i);
      EvaluationSetItem item = new EvaluationSetItem();
      item.setItemKey(String.valueOf(i));
      List<Turn> turns = new ArrayList<>();
      item.setTurns(turns);
      Turn turn = new Turn();
      turn.setId(0L);
      turns.add(turn);
      List<FieldData> fieldDatas = new ArrayList<>();
      turn.setFieldDataList(fieldDatas);
      for (FieldMappingDTO fieldMapping : fieldMappings) {
        String source = fieldMapping.getSource();
        String target = fieldMapping.getTarget();
        Object targetValue = importData.get(target);
        FieldData fieldData = new FieldData();
        fieldData.setKey(source);
        fieldData.setName(source);
        Content content = new Content();
        fieldData.setContent(content);
        content.setText(String.valueOf(targetValue));
        fieldDatas.add(fieldData);
      }
      // 如果通过所有校验，添加到成功列表
      items.add(item);
    }
    // 全量覆盖
    if (request.getOption().getOverwriteDataset()) {
      evaluationSetItemService.clearEvaluationSetDraftItem(request.getSpaceId(), request.getEvaluationSetId());
    }
    // 添加数据
    BatchCreateEvaluationSetItemsParam batchCreateEvaluationSetItemsParam = new BatchCreateEvaluationSetItemsParam();
    batchCreateEvaluationSetItemsParam.setSpaceId(request.getSpaceId());
    batchCreateEvaluationSetItemsParam.setEvaluationSetId(request.getEvaluationSetId());
    batchCreateEvaluationSetItemsParam.setItems(items);
    batchCreateEvaluationSetItemsParam.setSkipInvalidItems(false);
    batchCreateEvaluationSetItemsParam.setAllowPartialAdd(true);
    BatchCreateEvaluationSetItemsResult batchCreateEvaluationSetItemsResult = evaluationSetItemService.batchCreateEvaluationSetItems(batchCreateEvaluationSetItemsParam);
    List<ItemErrorGroup> errors = batchCreateEvaluationSetItemsResult.getErrors();
    int totalCount = items.size();
    int successCount = batchCreateEvaluationSetItemsResult.getIdMap().size();
    resp.setSuccessCount(successCount);
    resp.setFailCount(totalCount - successCount);
    resp.setTotalCount(totalCount);
    resp.setErrors(EvaluationSetItemApplicationConvertor.convertItemErrorGroupDO2DTOs(errors));
    return resp;
  }

}
