package com.iwhalecloud.bote.loop.evaluation.application;

import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoService;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRecordDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.EvaluatorApplicationService;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorRecordsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorRecordsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.CheckEvaluatorNameRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.CheckEvaluatorNameResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.CreateEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.CreateEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.DebugEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.DebugEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.DeleteEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.DeleteEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetDefaultPromptEvaluatorToolsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetDefaultPromptEvaluatorToolsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorRecordRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorRecordResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetTemplateInfoRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetTemplateInfoResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListEvaluatorVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListEvaluatorVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListEvaluatorsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListEvaluatorsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListTemplatesRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListTemplatesResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.RunEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.RunEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.SubmitEvaluatorVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.SubmitEvaluatorVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorDraftRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorDraftResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorRecordRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorRecordResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorResponse;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator.EvaluatorApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator.EvaluatorInputDataApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator.EvaluatorOutputDataApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator.EvaluatorRecordApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Correction;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorListResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorVersionListResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluatorRequest;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluatorVersionRequest;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.RunEvaluatorParam;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorRecordService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorService;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvaluatorApplicationServiceImpl implements EvaluatorApplicationService {
  private final EvaluatorService evaluatorService;
  private final EvaluatorRecordService evaluatorRecordService;
  private final UserInfoService userInfoService;

  @Override
  public ListEvaluatorsResponse listEvaluators(ListEvaluatorsRequest request) {
    ListEvaluatorRequest srvReq = buildSrvListEvaluatorRequest(request);
    EvaluatorListResult result = evaluatorService.listEvaluator(srvReq);
    // 返回结果构建
    List<EvaluatorDTO> dtoList = result.getEvaluators().stream()
      .map(EvaluatorApplicationConvertor::convertDO2DTO)
      .collect(Collectors.toList());

    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    userInfoCarriers.addAll(dtoList);
    userInfoService.packUserInfo(userInfoCarriers);

    return ListEvaluatorsResponse.builder()
      .total(result.getTotal())
      .evaluators(dtoList)
      .build();
  }

  @Override
  public BatchGetEvaluatorsResponse batchGetEvaluators(BatchGetEvaluatorsRequest request) {
    // 获取元信息和草稿
    List<Evaluator> drafts = evaluatorService.batchGetEvaluator(
      request.getWorkspaceId(),
      request.getEvaluatorIds(),
      request.getIncludeDeleted());

    if (drafts.isEmpty()) {
      return BatchGetEvaluatorsResponse.builder().build();
    }

    // TODO 鉴权

    // 返回结果构建
    List<EvaluatorDTO> dtoList = drafts.stream()
      .map(EvaluatorApplicationConvertor::convertDO2DTO)
      .collect(Collectors.toList());

    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    userInfoCarriers.addAll(dtoList);
    userInfoService.packUserInfo(userInfoCarriers);

    return BatchGetEvaluatorsResponse.builder()
      .evaluators(dtoList)
      .build();
  }

  @Override
  public GetEvaluatorResponse getEvaluator(GetEvaluatorRequest request) {
    // 获取对应草稿版本
    Evaluator draft = evaluatorService.getEvaluator(
      request.getWorkspaceId(),
      request.getEvaluatorId(),
      request.getIncludeDeleted());

    if (draft == null) {
      return GetEvaluatorResponse.builder().build();
    }
    // 返回结果构建
    EvaluatorDTO dto = EvaluatorApplicationConvertor.convertDO2DTO(draft);
    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    userInfoCarriers.add(dto);
    userInfoService.packUserInfo(userInfoCarriers);

    return GetEvaluatorResponse.builder()
      .evaluator(dto)
      .build();
  }

  @Override
  public CreateEvaluatorResponse createEvaluator(CreateEvaluatorRequest request) {
    // 校验参数
    validateCreateEvaluatorRequest(request);

    // TODO 鉴权

    // 转换请求参数为领域对象
    Evaluator evaluatorDO = EvaluatorApplicationConvertor.convertDTO2DO(request.getEvaluator());
    Long evaluatorId = evaluatorService.createEvaluator(evaluatorDO, request.getCid());

    // 返回创建结果
    return CreateEvaluatorResponse.builder()
      .evaluatorId(evaluatorId)
      .build();
  }

  @Override
  public UpdateEvaluatorResponse updateEvaluator(UpdateEvaluatorRequest request) {
    validateUpdateEvaluatorRequest(request);

    // TODO 鉴权
    Evaluator evaluatorDO = evaluatorService.getEvaluator(
      request.getWorkspaceId(), request.getEvaluatorId(), false);
    if (evaluatorDO == null) {
      throw new BssException("evaluator not exist");
    }

    // TODO: 机审逻辑

    // domain调用
    String userIDInContext = SessionContext.getCurrentUserId();
    evaluatorService.updateEvaluatorMeta(
      request.getEvaluatorId(),
      request.getWorkspaceId(),
      request.getName(),
      request.getDescription(),
      userIDInContext,
      request.getCatalogItemId());

    return UpdateEvaluatorResponse.builder().build();
  }

  @Override
  public UpdateEvaluatorDraftResponse updateEvaluatorDraft(UpdateEvaluatorDraftRequest request) {
    // TODO 鉴权
    Evaluator evaluatorDO = evaluatorService.getEvaluator(
      request.getWorkspaceId(), request.getEvaluatorId(), false);
    if (evaluatorDO == null) {
      throw new BssException("evaluator not exist");
    }

    // domain调用
    EvaluatorDTO evaluatorDTO = EvaluatorApplicationConvertor.convertDO2DTO(evaluatorDO);
    evaluatorDTO.getCurrentVersion().setEvaluatorContent(request.getEvaluatorContent());
    evaluatorDTO.setDraftSubmitted(false);

    evaluatorService.updateEvaluatorDraft(EvaluatorApplicationConvertor.convertDTO2DO(evaluatorDTO));

    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    userInfoCarriers.add(evaluatorDTO);
    userInfoService.packUserInfo(userInfoCarriers);

    return UpdateEvaluatorDraftResponse.builder()
      .evaluator(evaluatorDTO)
      .build();
  }

  @Override
  public DeleteEvaluatorResponse deleteEvaluator(DeleteEvaluatorRequest request) {
    // TODO 鉴权
    evaluatorService.batchGetEvaluator(
      request.getWorkspaceId(),
      List.of(request.getEvaluatorId()),
      false);

    // TODO: 批量鉴权逻辑

    // domain调用
    String userIDInContext = SessionContext.getCurrentUserId();
    evaluatorService.deleteEvaluator(List.of(request.getEvaluatorId()), userIDInContext);

    return DeleteEvaluatorResponse.builder().build();
  }

  @Override
  public ListEvaluatorVersionsResponse listEvaluatorVersions(ListEvaluatorVersionsRequest request) {
    // TODO 鉴权

    // domain调用
    ListEvaluatorVersionRequest srvReq = buildListEvaluatorVersionRequest(request);
    EvaluatorVersionListResult result = evaluatorService.listEvaluatorVersion(srvReq);

    // 转换结果集
    List<EvaluatorVersionDTO> dtoList = result.getEvaluatorVersions().stream()
      .map(EvaluatorApplicationConvertor::convertDO2DTO)
      .map(EvaluatorDTO::getCurrentVersion)
      .collect(Collectors.toList());

    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    userInfoCarriers.addAll(dtoList);
    userInfoService.packUserInfo(userInfoCarriers);

    return ListEvaluatorVersionsResponse.builder()
      .evaluatorVersions(dtoList)
      .total(result.getTotal())
      .build();
  }

  @Override
  public GetEvaluatorVersionResponse getEvaluatorVersion(GetEvaluatorVersionRequest request) {
    Evaluator evaluatorDO = evaluatorService.getEvaluatorVersion(
      request.getEvaluatorVersionId(), request.getIncludeDeleted());

    if (evaluatorDO == null) {
      return GetEvaluatorVersionResponse.builder().build();
    }

    // TODO 鉴权

    // 返回结果构建
    EvaluatorDTO dto = EvaluatorApplicationConvertor.convertDO2DTO(evaluatorDO);
    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    userInfoCarriers.add(dto);
    userInfoService.packUserInfo(userInfoCarriers);

    return GetEvaluatorVersionResponse.builder()
      .evaluator(dto)
      .build();
  }

  @Override
  public BatchGetEvaluatorVersionsResponse batchGetEvaluatorVersions(BatchGetEvaluatorVersionsRequest request) {
    List<Evaluator> evaluatorDOList = evaluatorService.batchGetEvaluatorVersion(
      request.getWorkspaceId(),
      request.getEvaluatorVersionIds(),
      request.getIncludeDeleted());

    if (evaluatorDOList.isEmpty()) {
      return BatchGetEvaluatorVersionsResponse.builder().build();
    }

    // 返回结果构建
    List<EvaluatorDTO> dtoList = evaluatorDOList.stream()
      .map(EvaluatorApplicationConvertor::convertDO2DTO)
      .collect(Collectors.toList());

    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    userInfoCarriers.addAll(dtoList);
    userInfoService.packUserInfo(userInfoCarriers);

    return BatchGetEvaluatorVersionsResponse.builder()
      .evaluators(dtoList)
      .build();
  }

  @Override
  public SubmitEvaluatorVersionResponse submitEvaluatorVersion(SubmitEvaluatorVersionRequest request) {
    // 校验参数
    validateSubmitEvaluatorVersionRequest(request);

    // TODO 鉴权
    Evaluator evaluatorDO = evaluatorService.getEvaluator(
      request.getWorkspaceId(), request.getEvaluatorId(), false);
    if (evaluatorDO == null) {
      throw new BssException("evaluator not exist");
    }

    // TODO: 机审逻辑

    // domain调用
    evaluatorDO = evaluatorService.submitEvaluatorVersion(
      evaluatorDO,
      request.getVersion(),
      request.getDescription(),
      request.getCid());

    return SubmitEvaluatorVersionResponse.builder()
      .evaluator(EvaluatorApplicationConvertor.convertDO2DTO(evaluatorDO))
      .build();
  }

  @Override
  public ListTemplatesResponse listTemplates(ListTemplatesRequest request) {
    // TODO: 实现模板列表逻辑
    return ListTemplatesResponse.builder()
      .builtinTemplateKeys(List.of())
      .build();
  }

  @Override
  public GetTemplateInfoResponse getTemplateInfo(GetTemplateInfoRequest request) {
    // TODO: 实现模板详情逻辑
    throw new BssException("builtin template not found");
  }

  @Override
  public RunEvaluatorResponse runEvaluator(RunEvaluatorRequest request) {
    Evaluator evaluatorDO = evaluatorService.getEvaluatorVersion(
      request.getEvaluatorVersionId(), false);

    if (evaluatorDO == null) {
      throw new BssException("evaluator not exist");
    }

    // TODO 鉴权

    // domain调用
    RunEvaluatorParam srvReq = buildRunEvaluatorRequest(evaluatorDO.getName(), request);
    EvaluatorRecord recordDO = evaluatorService.runEvaluator(srvReq);

    return RunEvaluatorResponse.builder()
      .record(EvaluatorRecordApplicationConvertor.convertDO2DTO(recordDO))
      .build();
  }

  @Override
  public DebugEvaluatorResponse debugEvaluator(DebugEvaluatorRequest request) {
    // TODO 鉴权

    // TODO: 权益检查逻辑

    // domain调用
    EvaluatorDTO dto = EvaluatorDTO.builder()
      .workspaceId(request.getWorkspaceId())
      .evaluatorType(request.getEvaluatorType())
      .currentVersion(EvaluatorVersionDTO.builder()
        .evaluatorContent(request.getEvaluatorContent())
        .build())
      .build();

    Evaluator doEntity = EvaluatorApplicationConvertor.convertDTO2DO(dto);
    EvaluatorInputData inputData = EvaluatorInputDataApplicationConvertor.convertDTO2DO(request.getInputData());
    EvaluatorOutputData outputData = evaluatorService.debugEvaluator(request.getWorkspaceId(), doEntity, inputData);

    return DebugEvaluatorResponse.builder()
      .evaluatorOutputData(EvaluatorOutputDataApplicationConvertor.convertDO2DTO(outputData))
      .build();
  }

  @Override
  public UpdateEvaluatorRecordResponse updateEvaluatorRecord(UpdateEvaluatorRecordRequest request) {
    EvaluatorRecord evaluatorRecord = evaluatorRecordService.getEvaluatorRecord(
      request.getEvaluatorRecordId(), false);

    if (evaluatorRecord == null) {
      throw new BssException("evaluator record not found");
    }

    // TODO 鉴权
    Evaluator evaluatorDO = evaluatorService.getEvaluatorVersion(
      evaluatorRecord.getEvaluatorVersionId(), false);

    if (evaluatorDO == null) {
      return UpdateEvaluatorRecordResponse.builder().build();
    }

    // TODO: 机审逻辑

    // domain调用
    Correction correctionDO = EvaluatorOutputDataApplicationConvertor.convertCorrectionDTO2DO(request.getCorrection());
    evaluatorRecordService.correctEvaluatorRecord(evaluatorRecord, correctionDO);

    return UpdateEvaluatorRecordResponse.builder()
      .record(EvaluatorRecordApplicationConvertor.convertDO2DTO(evaluatorRecord))
      .build();
  }

  @Override
  public GetEvaluatorRecordResponse getEvaluatorRecord(GetEvaluatorRecordRequest request) {
    EvaluatorRecord evaluatorRecord = evaluatorRecordService.getEvaluatorRecord(
      request.getEvaluatorRecordId(), request.getIncludeDeleted());

    if (evaluatorRecord == null) {
      return GetEvaluatorRecordResponse.builder().build();
    }

    // TODO 鉴权
    Evaluator evaluatorDO = evaluatorService.getEvaluatorVersion(
      evaluatorRecord.getEvaluatorVersionId(), request.getIncludeDeleted());

    if (evaluatorDO == null) {
      return GetEvaluatorRecordResponse.builder().build();
    }

    // 返回结果构建
    EvaluatorRecordDTO dto = EvaluatorRecordApplicationConvertor.convertDO2DTO(evaluatorRecord);
    List<UserInfoCarrier> userInfoCarriers = Lists.newArrayList();
    userInfoCarriers.add(dto);
    userInfoService.packUserInfo(userInfoCarriers);

    return GetEvaluatorRecordResponse.builder()
      .record(dto)
      .build();
  }

  @Override
  public BatchGetEvaluatorRecordsResponse batchGetEvaluatorRecords(BatchGetEvaluatorRecordsRequest request) {
    List<Long> evaluatorRecordIDs = request.getEvaluatorRecordIds();
    List<EvaluatorRecord> evaluatorRecords = evaluatorRecordService.batchGetEvaluatorRecord(
      evaluatorRecordIDs, request.getIncludeDeleted());

    if (evaluatorRecords.isEmpty()) {
      return BatchGetEvaluatorRecordsResponse.builder().build();
    }

    // TODO 鉴权

    // 返回结果构建
    List<EvaluatorRecordDTO> dtoList = evaluatorRecords.stream()
      .map(EvaluatorRecordApplicationConvertor::convertDO2DTO)
      .collect(Collectors.toList());

    return BatchGetEvaluatorRecordsResponse.builder()
      .records(dtoList)
      .build();
  }

  @Override
  public GetDefaultPromptEvaluatorToolsResponse getDefaultPromptEvaluatorTools(GetDefaultPromptEvaluatorToolsRequest request) {
    // TODO: 实现默认工具逻辑
    return GetDefaultPromptEvaluatorToolsResponse.builder()
      .tools(List.of())
      .build();
  }

  @Override
  public CheckEvaluatorNameResponse checkEvaluatorName(CheckEvaluatorNameRequest request) {
    // TODO 鉴权

    // domain调用
    boolean exist = evaluatorService.checkNameExist(
      request.getWorkspaceId(),
      request.getEvaluatorId(),
      request.getName());

    if (exist) {
      return CheckEvaluatorNameResponse.builder()
        .pass(false)
        .message(String.format("evaluator name %s already exists", request.getName()))
        .build();
    }

    return CheckEvaluatorNameResponse.builder()
      .pass(true)
      .build();
  }

  // 辅助方法
  private ListEvaluatorRequest buildSrvListEvaluatorRequest(ListEvaluatorsRequest request) {
    ListEvaluatorRequest srvReq = ListEvaluatorRequest.builder()
      .spaceId(request.getWorkspaceId())
      .searchName(request.getSearchName())
      .creatorIds(request.getCreatorIds())
      .pageSize(request.getPageSize())
      .pageNum(request.getPageNumber())
      .withVersion(request.getWithVersion())
      .catalogItemId(request.getCatalogItemId())
      .build();

    // 转换评估器类型
    if (request.getEvaluatorType() != null) {
      List<EvaluatorType> evaluatorType = request.getEvaluatorType().stream()
        .map(o -> EvaluatorType.fromValue(o.getValue()))
        .collect(Collectors.toList());
      srvReq.setEvaluatorType(evaluatorType);
    }
    else {
      srvReq.setEvaluatorType(List.of(EvaluatorType.CODE, EvaluatorType.PROMPT));
    }


    // 转换排序条件
    List<OrderBy> orderBys = request.getOrderBys() != null ? request.getOrderBys().stream()
      .map(ob -> OrderBy.builder()
        .field(ob.getField())
        .isAsc(ob.getIsAsc())
        .build())
      .collect(Collectors.toList()) : null;
    srvReq.setOrderBys(orderBys);

    return srvReq;
  }

  private ListEvaluatorVersionRequest buildListEvaluatorVersionRequest(ListEvaluatorVersionsRequest request) {
    ListEvaluatorVersionRequest req = ListEvaluatorVersionRequest.builder()
      .evaluatorId(request.getEvaluatorId())
      .queryVersions(request.getQueryVersions())
      .build();

    if (request.getPageSize() == null) {
      req.setPageSize(10); // 默认值
    }
    else {
      req.setPageSize(request.getPageSize());
    }

    if (request.getPageNumber() == null) {
      req.setPageNum(1); // 默认值
    }
    else {
      req.setPageNum(request.getPageNumber());
    }

    if (request.getOrderBys() == null || request.getOrderBys().isEmpty()) {
      req.setOrderBys(List.of(OrderBy.builder()
        .field("updated_at")
        .isAsc(false)
        .build()));
    }
    else {
      List<OrderBy> orderBy = request.getOrderBys().stream()
        .map(ob -> OrderBy.builder()
          .field(ob.getField())
          .isAsc(ob.getIsAsc())
          .build())
        .collect(Collectors.toList());
      req.setOrderBys(orderBy);
    }

    return req;
  }

  private RunEvaluatorParam buildRunEvaluatorRequest(String evaluatorName, RunEvaluatorRequest request) {
    return RunEvaluatorParam.builder()
      .spaceId(request.getWorkspaceId())
      .name(evaluatorName)
      .evaluatorVersionId(request.getEvaluatorVersionId())
      .experimentId(request.getExperimentId())
      .experimentRunId(request.getExperimentRunId())
      .itemId(request.getItemId())
      .turnId(request.getTurnId())
      .inputData(EvaluatorInputDataApplicationConvertor.convertDTO2DO(request.getInputData()))
      .build();
  }

  // 辅助方法
  private void validateCreateEvaluatorRequest(CreateEvaluatorRequest request) {
    validateRequestNotNull(request);
    EvaluatorDTO evaluator = request.getEvaluator();

    validateBasicFields(evaluator);
    validateEvaluatorType(evaluator);
    validateEvaluatorContent(evaluator);

    // TODO 机审
  }

  private void validateRequestNotNull(CreateEvaluatorRequest request) {
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getEvaluator() == null) {
      throw new BssException("evaluator is nil");
    }
  }

  private void validateBasicFields(EvaluatorDTO evaluator) {
    validateWorkspaceId(evaluator);
    validateName(evaluator);
    validateDescription(evaluator);
  }

  private void validateWorkspaceId(EvaluatorDTO evaluator) {
    if (evaluator.getWorkspaceId() == null) {
      throw new BssException("space id is nil");
    }
  }

  private void validateName(EvaluatorDTO evaluator) {
    if (evaluator.getName() == null || evaluator.getName().trim().isEmpty()) {
      throw new BssException("name is nil");
    }
    if (evaluator.getName().length() > 50) {
      throw new BssException("evaluator name exceeds max length");
    }
  }

  private void validateDescription(EvaluatorDTO evaluator) {
    if (evaluator.getDescription() != null && evaluator.getDescription().length() > 200) {
      throw new BssException("evaluator description exceeds max length");
    }
  }

  private void validateEvaluatorType(EvaluatorDTO evaluator) {
    if (evaluator.getEvaluatorType() == null) {
      throw new BssException("evaluator type is nil");
    }
  }

  private void validateEvaluatorContent(EvaluatorDTO evaluator) {
    if (evaluator.getCurrentVersion() == null) {
      throw new BssException("current version is nil");
    }

    if (evaluator.getCurrentVersion().getEvaluatorContent() == null) {
      throw new BssException("evaluator content is nil");
    }

    validateEvaluatorContentByType(evaluator);
  }

  private void validateEvaluatorContentByType(EvaluatorDTO evaluator) {
    EvaluatorTypeDTO evaluatorType = evaluator.getEvaluatorType();
    EvaluatorContentDTO content = evaluator.getCurrentVersion().getEvaluatorContent();

    if (EvaluatorTypeDTO.PROMPT.equals(evaluatorType)) {
      validatePromptEvaluator(content);
    }
    else if (EvaluatorTypeDTO.CODE.equals(evaluatorType)) {
      validateCodeEvaluator(content);
    }
  }

  private void validatePromptEvaluator(EvaluatorContentDTO content) {
    if (content.getPromptEvaluator() == null) {
      throw new BssException("prompt evaluator is nil");
    }
  }

  private void validateCodeEvaluator(EvaluatorContentDTO content) {
    if (content.getCodeEvaluator() == null) {
      throw new BssException("code evaluator is nil");
    }
  }

  private void validateUpdateEvaluatorRequest(UpdateEvaluatorRequest request) {
    if (request == null) {
      throw new BssException("req is nil");
    }

    // 校验评估器ID
    if (request.getEvaluatorId() == null || request.getEvaluatorId() == 0) {
      throw new BssException("id is 0");
    }

    // 校验工作空间ID
    if (request.getWorkspaceId() == null) {
      throw new BssException("space id is nil");
    }

    // 校验名称长度
    if (request.getName() != null && request.getName().length() > 50) {
      throw new BssException("evaluator name exceeds max length");
    }

    // 校验描述长度
    if (request.getDescription() != null && request.getDescription().length() > 200) {
      throw new BssException("evaluator description exceeds max length");
    }
  }

  private void validateSubmitEvaluatorVersionRequest(SubmitEvaluatorVersionRequest request) {
    if (request == null) {
      throw new BssException("req is nil");
    }

    // 校验评估器ID
    if (request.getEvaluatorId() == null || request.getEvaluatorId() == 0) {
      throw new BssException("evaluator id is empty");
    }

    // 校验版本号
    if (request.getVersion() == null || request.getVersion().trim().isEmpty()) {
      throw new BssException("evaluator version is empty");
    }

    // 校验版本号长度
    if (request.getVersion().length() > 50) {
      throw new BssException("evaluator version exceeds max length");
    }

    // 校验版本号格式（SemVer规范）
    if (!isValidSemVer(request.getVersion())) {
      throw new BssException("evaluator version does not follow SemVer specification");
    }

    // 校验描述长度
    if (request.getDescription() != null && request.getDescription().length() > 200) {
      throw new BssException("evaluator version description exceeds max length");
    }

    // TODO: 机审
  }

  /**
   * 校验版本号是否符合SemVer规范
   *
   * @param version 版本号
   * @return 是否符合规范
   */
  private boolean isValidSemVer(String version) {
    if (version == null || version.trim().isEmpty()) {
      return false;
    }

    // SemVer格式：MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]
    // 例如：1.0.0, 1.0.0-alpha, 1.0.0-alpha.1, 1.0.0+20130313144700
    String semVerPattern = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";
    return version.matches(semVerPattern);
  }
}
