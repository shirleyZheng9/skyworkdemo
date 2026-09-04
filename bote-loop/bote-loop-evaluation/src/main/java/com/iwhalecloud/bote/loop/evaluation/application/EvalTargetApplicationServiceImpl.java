package com.iwhalecloud.bote.loop.evaluation.application;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetRecordDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.EvalTargetApplicationService;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetRecordsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetRecordsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetsBySourceRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetsBySourceResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetSourceEvalTargetsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetSourceEvalTargetsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.CreateEvalTargetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.CreateEvalTargetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ExecuteEvalTargetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ExecuteEvalTargetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.GetEvalTargetRecordRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.GetEvalTargetRecordResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.GetEvalTargetVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.GetEvalTargetVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetsResponse;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaltarget.EvalTargetConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaltarget.EvalTargetRecordConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvalTargetBySourceReqParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BotInfoType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateEvalTargetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetCreateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExecuteTargetCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IEvalTargetService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ISourceEvalTargetOperateService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class EvalTargetApplicationServiceImpl implements EvalTargetApplicationService {
  private final IEvalTargetService evalTargetService;
  private final Map<EvalTargetType, ISourceEvalTargetOperateService> typedOperators;

  public EvalTargetApplicationServiceImpl(IEvalTargetService evalTargetService, List<ISourceEvalTargetOperateService> sourceEvalTargetOperateServices) {
    this.evalTargetService = evalTargetService;
    this.typedOperators = sourceEvalTargetOperateServices.stream()
      .collect(Collectors.toUnmodifiableMap(ISourceEvalTargetOperateService::evalType, Function.identity()));
  }

  @Override
  public CreateEvalTargetResponse createEvalTarget(CreateEvalTargetRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getParam() == null) {
      throw new BssException("req param is nil");
    }
    if (request.getParam().getSourceTargetId() == null) {
      throw new BssException("source target id is nil");
    }
    if (request.getParam().getSourceTargetVersion() == null) {
      throw new BssException("source target version is nil");
    }
    if (request.getParam().getEvalTargetType() == null) {
      throw new BssException("source target type is nil");
    }

    // TODO 鉴权

    // domain调用
    CreateEvalTargetParam param = CreateEvalTargetParam.builder()
      .sourceTargetId(request.getParam().getSourceTargetId())
      .sourceTargetVersion(request.getParam().getSourceTargetVersion())
      .evalTargetType(EvalTargetType.fromValue(request.getParam().getEvalTargetType().getValue()))
      .botInfoType(BotInfoType.fromValue(request.getParam().getBotInfoType().getValue()))
      .botPublishVersion(request.getParam().getBotPublishVersion())
      .build();

    EvalTargetCreateResult result = evalTargetService.createEvalTarget(request.getWorkspaceId(), param.getSourceTargetId(), param.getSourceTargetVersion(), param.getEvalTargetType(), null, null);

    // 返回结果构建
    return CreateEvalTargetResponse.builder()
      .id(result.getId())
      .versionId(result.getVersionId())
      .build();
  }

  @Override
  public BatchGetEvalTargetsBySourceResponse batchGetEvalTargetsBySource(BatchGetEvalTargetsBySourceRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getSourceTargetIds() == null || request.getSourceTargetIds().isEmpty()) {
      throw new BssException("source target id is nil");
    }
    if (request.getEvalTargetType() == null) {
      throw new BssException("source target type is nil");
    }

    // TODO 鉴权

    // domain调用
    BatchGetEvalTargetBySourceReqParam param = BatchGetEvalTargetBySourceReqParam.builder()
      .spaceId(request.getWorkspaceId())
      .sourceTargetId(request.getSourceTargetIds())
      .targetType(EvalTargetType.fromValue(request.getEvalTargetType().getValue()))
      .build();

    List<EvalTarget> evalTargets = evalTargetService.batchGetEvalTargetBySource(param);

    if (evalTargets.isEmpty()) {
      return BatchGetEvalTargetsBySourceResponse.builder().build();
    }
    // 返回结果构建
    List<EvalTargetDTO> res = evalTargets.stream()
      .map(EvalTargetConvertor::convertDO2DTO)
      .collect(Collectors.toList());

    return BatchGetEvalTargetsBySourceResponse.builder()
      .evalTargets(res)
      .build();
  }

  @Override
  public GetEvalTargetVersionResponse getEvalTargetVersion(GetEvalTargetVersionRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getEvalTargetVersionId() == null) {
      throw new BssException("target version id is nil");
    }

    // domain调用
    EvalTarget evalTarget = evalTargetService.getEvalTargetVersion(
      request.getWorkspaceId(), request.getEvalTargetVersionId(), false);

    if (evalTarget == null) {
      return GetEvalTargetVersionResponse.builder().build();
    }

    // TODO 鉴权

    // 返回结果构建
    return GetEvalTargetVersionResponse.builder()
      .evalTarget(EvalTargetConvertor.convertDO2DTO(evalTarget))
      .build();
  }

  @Override
  public BatchGetEvalTargetVersionsResponse batchGetEvalTargetVersions(BatchGetEvalTargetVersionsRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getEvalTargetVersionIds() == null || request.getEvalTargetVersionIds().isEmpty()) {
      throw new BssException("target ids is nil");
    }

    // TODO 鉴权

    // domain调用
    List<EvalTarget> evalTargets = evalTargetService.batchGetEvalTargetVersion(
      request.getWorkspaceId(), request.getEvalTargetVersionIds(),
      Boolean.TRUE.equals(request.getNeedSourceInfo()));

    if (evalTargets.isEmpty()) {
      return BatchGetEvalTargetVersionsResponse.builder().build();
    }

    // 返回结果构建
    List<EvalTargetDTO> res = evalTargets.stream()
      .map(EvalTargetConvertor::convertDO2DTO)
      .collect(Collectors.toList());

    return BatchGetEvalTargetVersionsResponse.builder()
      .evalTargets(res)
      .build();
  }

  @Override
  public ListSourceEvalTargetsResponse listSourceEvalTargets(ListSourceEvalTargetsRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getTargetType() == null) {
      throw new BssException("target type is nil");
    }

    // TODO 鉴权

    // domain调用
    ListSourceParam param = ListSourceParam.builder()
      .spaceId(request.getWorkspaceId())
      .pageSize(request.getPageSize())
      .cursor(request.getPageToken())
      .keyWord(request.getName())
      .targetType(EvalTargetType.fromValue(request.getTargetType().getValue()))
      .build();

    ISourceEvalTargetOperateService operator = typedOperators.get(param.getTargetType());
    if (operator == null) {
      throw new BssException("不支持的评测对象类型: " + param.getTargetType());
    }
    ListSourceResult result = operator.listSource(param);

    // 返回结果构建
    List<EvalTargetDTO> dtos = result.getEvalTargets().stream()
      .map(EvalTargetConvertor::convertDO2DTO)
      .collect(Collectors.toList());

    return ListSourceEvalTargetsResponse.builder()
      .evalTargets(dtos)
      .nextPageToken(result.getNextCursor())
      .hasMore(result.getHasMore())
      .build();
  }

  @Override
  public ListSourceEvalTargetVersionsResponse listSourceEvalTargetVersions(ListSourceEvalTargetVersionsRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getTargetType() == null) {
      throw new BssException("target type is nil");
    }

    // TODO 鉴权

    // domain调用
    ListSourceVersionParam param = ListSourceVersionParam.builder()
      .spaceId(request.getWorkspaceId())
      .pageSize(request.getPageSize())
      .cursor(request.getPageToken())
      .sourceTargetId(request.getSourceTargetId())
      .targetType(EvalTargetType.fromValue(request.getTargetType().getValue()))
      .build();

    ISourceEvalTargetOperateService operator = typedOperators.get(param.getTargetType());
    if (operator == null) {
      throw new BssException("不支持的评测对象类型: " + param.getTargetType());
    }
    ListSourceVersionResult result = operator.listSourceVersion(param);

    // 返回结果构建
    List<EvalTargetVersionDTO> dtos = result.getVersions().stream()
      .map(EvalTargetConvertor::convertVersionDO2DTO)
      .collect(Collectors.toList());

    return ListSourceEvalTargetVersionsResponse.builder()
      .versions(dtos)
      .nextPageToken(result.getNextCursor())
      .hasMore(result.getHasMore())
      .build();
  }

  @Override
  public ExecuteEvalTargetResponse executeEvalTarget(ExecuteEvalTargetRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getInputData() == null) {
      throw new BssException("inputData is nil");
    }

    // TODO 鉴权

    // domain调用
    ExecuteTargetCtx ctx = ExecuteTargetCtx.builder()
      .experimentRunId(request.getExperimentRunId())
      .itemId(0L)
      .turnId(0L)
      .build();

    EvalTargetRecord targetRecord = evalTargetService.executeTarget(
      request.getWorkspaceId(),
      request.getEvalTargetId(),
      request.getEvalTargetVersionId(),
      ctx,
      EvalTargetRecordConvertor.convertInputDTO2DO(request.getInputData()));

    // 返回结果构建
    return ExecuteEvalTargetResponse.builder()
      .evalTargetRecord(EvalTargetRecordConvertor.convertDO2DTO(targetRecord))
      .build();
  }

  @Override
  public GetEvalTargetRecordResponse getEvalTargetRecord(GetEvalTargetRecordRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // domain调用
    EvalTargetRecord targetRecord = evalTargetService.getRecordById(
      request.getWorkspaceId(), request.getEvalTargetRecordId());

    if (targetRecord == null) {
      return GetEvalTargetRecordResponse.builder().build();
    }

    // TODO 鉴权

    // 返回结果构建
    return GetEvalTargetRecordResponse.builder()
      .evalTargetRecord(EvalTargetRecordConvertor.convertDO2DTO(targetRecord))
      .build();
  }

  @Override
  public BatchGetEvalTargetRecordsResponse batchGetEvalTargetRecords(BatchGetEvalTargetRecordsRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }

    // TODO 鉴权

    // domain调用
    List<EvalTargetRecord> recordList = evalTargetService.batchGetRecordByIds(
      request.getWorkspaceId(), request.getEvalTargetRecordIds());

    // 返回结果构建
    List<EvalTargetRecordDTO> dtoList = recordList.stream()
      .map(EvalTargetRecordConvertor::convertDO2DTO)
      .collect(Collectors.toList());

    return BatchGetEvalTargetRecordsResponse.builder()
      .evalTargetRecords(dtoList)
      .build();
  }

  @Override
  public BatchGetSourceEvalTargetsResponse batchGetSourceEvalTargets(BatchGetSourceEvalTargetsRequest request) {
    // 参数校验
    if (request == null) {
      throw new BssException("req is nil");
    }
    if (request.getTargetType() == null) {
      throw new BssException("target type is nil");
    }

    // TODO 鉴权

    // domain调用
    EvalTargetType targetType = EvalTargetType.fromValue(request.getTargetType().getValue());
    ISourceEvalTargetOperateService operator = typedOperators.get(targetType);
    if (operator == null) {
      throw new BssException("不支持的评测对象类型: " + targetType);
    }
    List<EvalTarget> res = operator.batchGetSource(
      request.getWorkspaceId(), request.getSourceTargetIds());

    // 返回结果构建
    List<EvalTargetDTO> dtos = res.stream()
      .map(EvalTargetConvertor::convertDO2DTO)
      .collect(Collectors.toList());

    return BatchGetSourceEvalTargetsResponse.builder()
      .evalTargets(dtos)
      .build();
  }
}
