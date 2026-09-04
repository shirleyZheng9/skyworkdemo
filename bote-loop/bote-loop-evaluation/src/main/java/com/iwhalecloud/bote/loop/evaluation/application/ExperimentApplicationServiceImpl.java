package com.iwhalecloud.bote.loop.evaluation.application;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.EvaluatorFieldMappingDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentFilterDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptAggregateResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptRetryModeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptStatsInfoDTO;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.CreateEvalTargetParamDTO;
import com.iwhalecloud.bote.loop.client.evaluation.expt.ExperimentApplicationService;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchDeleteExperimentsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchDeleteExperimentsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentAggrResultRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentAggrResultResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentResultRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentResultResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CheckExperimentNameRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CheckExperimentNameResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CloneExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CloneExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CreateExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CreateExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.DeleteExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.DeleteExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.FinishExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.FinishExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.InvokeExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.InvokeExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.KillExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.KillExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.ListExperimentStatsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.ListExperimentsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.RetryExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.RetryExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.RunExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.RunExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.SubmitExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.SubmitExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpdateExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpdateExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpsertExptTurnResultFilterRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpsertExptTurnResultFilterResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpsertExptTurnResultFilterTypeDTO;
import com.iwhalecloud.bote.loop.domain.component.rpc.IUserProvider;
import com.iwhalecloud.bote.loop.domain.component.rpc.dto.RpcUserInfo;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset.EvaluationSetItemApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.experiment.ExperimentAggrResultApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.experiment.ExperimentApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.experiment.ExperimentFilterApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.experiment.ExperimentResultApplicationConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchCreateEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchCreateEvaluationSetItemsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CompleteExptOption;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateExptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExperimentResultListResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggregateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterAccelerator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.InvokeExptReq;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListExptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.MGetExperimentResultParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.pdf.ExptResultData;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetItemService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptAggrResultService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptResultService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IExptManager;
import com.iwhalecloud.bote.loop.evaluation.domain.service.impl.ExperimentOpenPdfServiceImpl;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class ExperimentApplicationServiceImpl implements ExperimentApplicationService {
  private static final Logger logger = LoggerFactory.getLogger(ExperimentApplicationService.class);
  private final IExptManager exptManager;
  private final ExptResultService resultSvc;
  private final EvaluationSetItemService evaluationSetItemService;
  private final ExptAggrResultService exptAggrResultService;
  private final IIDGenerator iidGenerator;
  private final IUserProvider userProvider;
  private final ExperimentOpenPdfServiceImpl experimentOpenPdfService;

  @Override
  public CreateExperimentResponse createExperiment(CreateExperimentRequest request) {
    CreateEvalTargetParamDTO createEvalTargetParam = request.getCreateEvalTargetParam();
    Assert.notNull(createEvalTargetParam, "create eval target param is null");
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();
    if (request.getSession() != null && request.getSession().getUserId() != null) {
      session = Session.builder()
        .userId(String.valueOf(request.getSession().getUserId()))
        .build();
    }
    // domain调用
    CreateExptParam param = ExperimentApplicationConvertor.convertCreateReq(request);
    Experiment createExpt = exptManager.createExpt(param, session);

    return CreateExperimentResponse.builder()
      .experiment(ExperimentApplicationConvertor.toExptDTO(createExpt))
      .build();
  }

  @Override
  public SubmitExperimentResponse submitExperiment(SubmitExperimentRequest request) {
    if (hasDuplicates(request.getEvaluatorVersionIds())) {
      throw new BssException("评估器版本重复，请检查");
    }
    // 创建实验
    CreateExperimentResponse cresp = createExperiment(CreateExperimentRequest.builder()
      .workspaceId(request.getWorkspaceId())
      .evalSetVersionId(request.getEvalSetVersionId())
      .evalSetId(request.getEvalSetId())
      .evaluatorVersionIds(request.getEvaluatorVersionIds())
      .name(request.getName())
      .desc(request.getDesc())
      .targetFieldMapping(request.getTargetFieldMapping())
      .evaluatorFieldMapping(request.getEvaluatorFieldMapping())
      .itemConcurNum(request.getItemConcurNum())
      .evaluatorsConcurNum(request.getEvaluatorsConcurNum())
      .createEvalTargetParam(request.getCreateEvalTargetParam())
      .exptType(request.getExptType())
      .maxAliveTime(request.getMaxAliveTime())
      .sourceType(request.getSourceType())
      .sourceId(request.getSourceId())
      .session(request.getSession())
      .catalogItemId(request.getCatalogItemId())
      .build());

    // 异步执行，避免拖慢前台返回
    ThreadPools.getEval().submit(() -> {
      try {
        runExperiment(RunExperimentRequest.builder()
          .workspaceId(request.getWorkspaceId())
          .exptId(cresp.getExperiment().getId())
          .exptType(request.getExptType())
          .session(request.getSession())
          .ext(request.getExt())
          .build());
      }
      catch (Exception e) {
        logger.error("Failed to invoke llm client interceptor", e);
      }
    });
    // 运行实验
    return SubmitExperimentResponse.builder()
      .experiment(cresp.getExperiment())
      .runId(null)
      .build();
  }

  @Override
  public CheckExperimentNameResponse checkExperimentName(CheckExperimentNameRequest request) {
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();
    boolean pass = exptManager.checkName(request.getName(), request.getWorkspaceId(), session);
    String message = null;
    if (!pass) {
      message = String.format("experiment name %s already exist", request.getName());
    }

    return CheckExperimentNameResponse.builder()
      .pass(pass)
      .message(message)
      .build();
  }

  @Override
  public BatchGetExperimentsResponse batchGetExperiments(BatchGetExperimentsRequest request) {
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();
    List<Experiment> dos = exptManager.mGetDetail(request.getExptIds(), request.getWorkspaceId(), session);
    List<ExperimentDTO> dtos = ExperimentApplicationConvertor.toExptDTOs(dos);
    fillUserInfo(dtos);
    return BatchGetExperimentsResponse.builder()
      .experiments(dtos)
      .build();
  }

  @Override
  public PageInfo<ExperimentDTO> listExperiments(ListExperimentsRequest request) {
    ListExptParam listExptParam = buildListExptParam(request);
    // domain调用
    PageInfo<Experiment> result = exptManager.list(listExptParam);

    PageInfo<ExperimentDTO> pageInfoDto = result.convert(ExperimentApplicationConvertor::toExptDTO);
    List<ExperimentDTO> dtos = pageInfoDto.getList();
    fillUserInfo(dtos);
    return pageInfoDto;
  }

  private ListExptParam buildListExptParam(ListExperimentsRequest request) {
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();
    // 转换过滤条件
    ExptListFilter filters = ExperimentFilterApplicationConvertor.convert(request.getFilterOption(), request.getWorkspaceId());
    // 转换排序条件
    List<OrderBy> orderBys = request.getOrderBys() != null ? request.getOrderBys().stream()
      .map(e -> {
        return OrderBy.builder()
          .field(e.getField())
          .isAsc(e.getIsAsc())
          .build();
      })
      .collect(Collectors.toList()) : null;
    String catalogItemId = request.getCatalogItemId();
    ListExptParam listExptParam = new ListExptParam();
    listExptParam.setPageNumber(request.getPageNumber());
    listExptParam.setPageSize(request.getPageSize());
    listExptParam.setSpaceId(request.getWorkspaceId());
    listExptParam.setFilter(filters);
    listExptParam.setOrders(orderBys);
    listExptParam.setSession(session);
    listExptParam.setCatalogItemId(request.getCatalogItemId() == null ? null : Long.parseLong(catalogItemId));
    return listExptParam;
  }

  private void fillUserInfo(List<ExperimentDTO> dtos) {
    List<String> userIds = dtos.stream().filter(o -> StringUtils.isNotEmpty(o.getCreatorBy())).map(o -> o.getCreatorBy()).toList();
    List<RpcUserInfo> userInfos = userProvider.mGetUserInfo(userIds);
    Map<String, RpcUserInfo> userInfMap = Maps.newHashMap();
    for (RpcUserInfo userInfo : userInfos) {
      userInfMap.put(userInfo.getUserId(), userInfo);
    }
    for (ExperimentDTO dto : dtos) {
      String creatorBy = dto.getCreatorBy();
      if (StringUtils.isEmpty(creatorBy)) {
        continue;
      }
      RpcUserInfo rpcUserInfo = userInfMap.get(creatorBy);
      if (rpcUserInfo == null) {
        continue;
      }
      dto.setCreatorByName(rpcUserInfo.getUserName());
    }
  }

  @Override
  public PageInfo<ExptStatsInfoDTO> listExperimentStats(ListExperimentStatsRequest request) {
    // 转换过滤条件
    ExptListFilter filters = ExperimentFilterApplicationConvertor.convert(request.getFilterOption(), request.getWorkspaceId());
    // domain调用
    PageInfo<Experiment> exptResult = exptManager.listExptRaw(
      request.getPageNumber(),
      request.getPageSize(),
      request.getWorkspaceId(),
      filters);
    List<Long> exptIDs = exptResult.getList().stream()
      .map(Experiment::getId)
      .collect(Collectors.toList());
    List<ExptStats> stats = resultSvc.mGetStats(exptIDs, request.getWorkspaceId(), null);
    Map<Long, ExptStats> exptID2Stats = stats.stream()
      .collect(Collectors.toMap(ExptStats::getExptId, e -> e));
    return exptResult.convert(exptDO -> ExperimentApplicationConvertor.toExptStatsInfoDTO(exptDO, exptID2Stats.get(exptDO.getId())));
  }

  @Override
  public UpdateExperimentResponse updateExperiment(UpdateExperimentRequest request) {
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();

    Experiment got = exptManager.get(request.getExptId(), request.getWorkspaceId(), session);

    if (!got.getName().equals(request.getName())) {
      boolean pass = exptManager.checkName(request.getName(), request.getWorkspaceId(), session);
      if (!pass) {
        throw new BssException(String.format("name %s already exist", request.getName()));
      }
    }

    // domain调用
    exptManager.update(Experiment.builder()
      .id(request.getExptId())
      .spaceId(request.getWorkspaceId())
      .name(request.getName())
      .description(request.getDesc())
      .catalogItemId(request.getCatalogItemId())
      .build(), session);

    Experiment resp = exptManager.get(request.getExptId(), request.getWorkspaceId(), session);

    return UpdateExperimentResponse.builder()
      .experiment(ExperimentApplicationConvertor.toExptDTO(resp))
      .build();
  }

  @Override
  public DeleteExperimentResponse deleteExperiment(DeleteExperimentRequest request) {
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();
    // domain调用
    exptManager.delete(request.getExptId(), request.getWorkspaceId(), session);

    return DeleteExperimentResponse.builder().build();
  }

  @Override
  public BatchDeleteExperimentsResponse batchDeleteExperiments(BatchDeleteExperimentsRequest request) {
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();

    // domain调用
    exptManager.mDelete(request.getExptIds(), request.getWorkspaceId(), session);

    return BatchDeleteExperimentsResponse.builder().build();
  }

  @Override
  public CloneExperimentResponse cloneExperiment(CloneExperimentRequest request) {
    Experiment exptDO = exptManager.clone(request.getExptId(), request.getWorkspaceId());
    // 创建统计记录
    Long id = iidGenerator.genId();
    resultSvc.createStats(ExptStats.builder()
      .id(id)
      .spaceId(request.getWorkspaceId())
      .exptId(exptDO.getId())
      .build());
    return CloneExperimentResponse.builder()
      .experiment(ExperimentApplicationConvertor.toExptDTO(exptDO))
      .build();
  }

  @Override
  public RunExperimentResponse runExperiment(RunExperimentRequest request) {
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();

    if (request.getSession() != null && request.getSession().getUserId() != null) {
      session = Session.builder()
        .userId(String.valueOf(request.getSession().getUserId()))
        .build();
    }

    Long runID = iidGenerator.genId();

    ExptRunMode evalMode = ExperimentApplicationConvertor.exptType2EvalMode(request.getExptType());

    // 记录运行
    exptManager.logRun(request.getExptId(), runID, evalMode, request.getWorkspaceId(), session);

    // 运行实验
    exptManager.run(request.getExptId(), runID, request.getWorkspaceId(), session, evalMode, request.getExt());

    return RunExperimentResponse.builder()
      .runId(runID)
      .build();
  }

  @Override
  public RetryExperimentResponse retryExperiment(RetryExperimentRequest request) {
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();
    Long runID = iidGenerator.genId();
    // 异步执行，避免拖慢前台返回
    ThreadPools.getEval().submit(() -> {
      List<Long> exptIds = resolveExperimentIds(request, session);
      ExptRunMode runMode = retryMode2EvalMode(request.getRetryMode());
      int retryTimes = calculateRetryTimes(request);
      executeRetryExperiments(exptIds, request, session, runMode, retryTimes);
    });

    return RetryExperimentResponse.builder()
      .runId(runID)
      .build();
  }

  /**
   * 解析实验ID列表
   */
  private List<Long> resolveExperimentIds(RetryExperimentRequest request, Session session) {
    List<Long> exptIds = request.getExptIds();
    if (exptIds == null) {
      exptIds = new ArrayList<>();
      exptIds.add(request.getExptId());
    }
    // 如果提供了目录ID，则根据目录ID查询实验ID列表
    if (request.getCatalogItemId() != null) {
      exptIds = queryExperimentIdsByCatalog(request, session);
    }
    return exptIds;
  }

  /**
   * 根据目录ID查询实验ID列表
   */
  private List<Long> queryExperimentIdsByCatalog(RetryExperimentRequest request, Session session) {
    ExptListFilter filter = ExptListFilter.builder().build();
    ListExptParam listExptParam = new ListExptParam();
    listExptParam.setPageNumber(1);
    listExptParam.setPageSize(10000);
    listExptParam.setSpaceId(request.getWorkspaceId());
    listExptParam.setFilter(filter);
    listExptParam.setSession(session);
    listExptParam.setCatalogItemId(request.getCatalogItemId());
    PageInfo<Experiment> exptPageInfo = exptManager.list(listExptParam);
    return exptPageInfo.getList().stream()
      .map(Experiment::getId)
      .toList();
  }

  /**
   * 计算重跑次数
   */
  private int calculateRetryTimes(RetryExperimentRequest request) {
    return (request.getTurn() != null && request.getTurn() > 0) ? request.getTurn() : 1;
  }

  /**
   * 执行重跑实验
   */
  private void executeRetryExperiments(List<Long> exptIds, RetryExperimentRequest request,
                                       Session session, ExptRunMode runMode, int retryTimes) {
    if (runMode.equals(ExptRunMode.ALL_RETRY)) {
      executeAllRetryMode(exptIds, request, session, runMode, retryTimes);
    }
    else {
      executeItemRetryMode(request, session, runMode, retryTimes);
    }
  }

  /**
   * 执行全部重跑模式
   */
  private void executeAllRetryMode(List<Long> exptIds, RetryExperimentRequest request,
                                   Session session, ExptRunMode runMode, int retryTimes) {
    for (Long exptId : exptIds) {
      for (int i = 0; i < retryTimes; i++) {
        Long currentRunID = iidGenerator.genId();
        exptManager.logRun(exptId, currentRunID, ExptRunMode.FAIL_RETRY, request.getWorkspaceId(), session);
        Map<String, String> ext = buildExtMap(request.getExt());
        exptManager.run(exptId, currentRunID, request.getWorkspaceId(), session, runMode, ext);
      }
    }
  }

  /**
   * 执行指定数据项重跑模式
   */
  private void executeItemRetryMode(RetryExperimentRequest request, Session session,
                                    ExptRunMode runMode, int retryTimes) {
    for (int i = 0; i < retryTimes; i++) {
      Long currentRunID = iidGenerator.genId();
      Map<String, String> ext = buildExtMapForItemRetry(request);
      exptManager.logRun(request.getExptId(), currentRunID, ExptRunMode.FAIL_RETRY, request.getWorkspaceId(), session);
      exptManager.run(request.getExptId(), currentRunID, request.getWorkspaceId(), session, runMode, ext);
    }
  }

  /**
   * 构建扩展信息Map
   */
  private Map<String, String> buildExtMap(Map<String, String> originalExt) {
    return originalExt != null ? new HashMap<>(originalExt) : new HashMap<>();
  }

  /**
   * 构建指定数据项重跑的扩展信息Map
   */
  private Map<String, String> buildExtMapForItemRetry(RetryExperimentRequest request) {
    Map<String, String> ext = buildExtMap(request.getExt());
    if (request.getItemIds() != null) {
      ext.put("itemIds", JsonUtil.toJsonString(request.getItemIds()));
    }
    // 每次重跑时，turn固定为1
    ext.put("turn", "1");
    return ext;
  }

  private ExptRunMode retryMode2EvalMode(ExptRetryModeDTO retryMode) {
    return switch (retryMode) {
      case RETRY_FAILURE -> ExptRunMode.FAIL_RETRY;
      case RETRY_TARGET_ITEMS -> ExptRunMode.ITEM_RETRY;
      default -> ExptRunMode.ALL_RETRY;
    };
  }

  @Override
  public KillExperimentResponse killExperiment(KillExperimentRequest request) {
    Session session = Session.builder()
      .userId(SessionContext.getCurrentUserId())
      .build();
    // 完成实验
    exptManager.completeExpt(request.getExptId(), request.getWorkspaceId(), session,
      CompleteExptOption.withStatus(ExptStatus.TERMINATED));

    return KillExperimentResponse.builder().build();
  }

  @Override
  public BatchGetExperimentResultResponse batchGetExperimentResult(BatchGetExperimentResultRequest request) {
    Page page = Page.builder()
      .offset(request.getPageNumber())
      .limit(request.getPageSize())
      .build();
    MGetExperimentResultParam param = MGetExperimentResultParam.builder()
      .spaceId(request.getWorkspaceId())
      .exptIds(request.getExperimentIds())
      .baseExptId(request.getBaselineExperimentId())
      .page(page)
      .itemIds(request.getItemIds())
      .useAccelerator(request.getUseAccelerator())
      .build();
    // 构建过滤条件
    buildExptTurnResultFilter(request, param);
    // domain调用
    ExperimentResultListResult result = resultSvc.mGetExperimentResult(param);
    return BatchGetExperimentResultResponse.builder()
      .columnEvalSetFields(ExperimentResultApplicationConvertor.convertColumnEvalSetFieldsDO2DTOs(result.getColumnEvalSetFields()))
      .columnEvaluators(ExperimentResultApplicationConvertor.convertColumnEvaluatorsDO2DTOs(result.getColumnEvaluators()))
      .total(result.getTotal())
      .itemResults(ExperimentResultApplicationConvertor.convertItemResultsDO2DTOs(result.getItemResults()))
      .build();
  }


  @Override
  public BatchGetExperimentAggrResultResponse batchGetExperimentAggrResult(BatchGetExperimentAggrResultRequest request) {
    List<ExptAggregateResult> aggrResults = exptAggrResultService.batchGetExptAggrResultByExperimentIds(
      request.getWorkspaceId(), request.getExperimentIds());
    List<ExptAggregateResultDTO> exptAggregateResultDTOs = aggrResults.stream()
      .map(ExperimentAggrResultApplicationConvertor::convertDOToDTO)
      .collect(Collectors.toList());
    List<Experiment> experiments = exptManager.mGet(request.getExperimentIds(), request.getWorkspaceId());
    packExperimentInfo(experiments, exptAggregateResultDTOs);
    return BatchGetExperimentAggrResultResponse.builder()
      .exptAggregateResults(exptAggregateResultDTOs)
      .build();
  }

  private void packExperimentInfo(List<Experiment> experiments, List<ExptAggregateResultDTO> exptAggregateResultDTOs) {
    Map<Long, Experiment> experimentMap = Maps.newHashMap();
    for (Experiment experiment : experiments) {
      experimentMap.put(experiment.getId(), experiment);
    }
    for (ExptAggregateResultDTO exptAggregateResultDTO : exptAggregateResultDTOs) {
      Long experimentId = exptAggregateResultDTO.getExperimentId();
      Experiment experiment = experimentMap.get(experimentId);
      if (experiment != null) {
        String name = experiment.getName();
        exptAggregateResultDTO.setExperimentName(name);
      }
    }
  }

  @Override
  public InvokeExperimentResponse invokeExperiment(InvokeExperimentRequest request) {
    Session session = Session.builder()
      .userId(String.valueOf(request.getSession().getUserId()))
      .build();

    Experiment got = exptManager.get(request.getExperimentId(), request.getWorkspaceId(), session);

    if (got.getStatus() != ExptStatus.PROCESSING && got.getStatus() != ExptStatus.PENDING) {
      throw new BssException("expt status not allow to invoke");
    }

    // 转换数据项
    List<EvaluationSetItem> itemDOS = EvaluationSetItemApplicationConvertor.convertItemDTO2DOs(request.getItems());

    // 批量创建评估集数据项
    BatchCreateEvaluationSetItemsParam batchCreateParam = BatchCreateEvaluationSetItemsParam.builder()
      .spaceId(request.getWorkspaceId())
      .evaluationSetId(request.getEvaluationSetId())
      .items(itemDOS)
      .skipInvalidItems(request.getSkipInvalidItems())
      .allowPartialAdd(request.getAllowPartialAdd())
      .build();

    BatchCreateEvaluationSetItemsResult batchResult = evaluationSetItemService.batchCreateEvaluationSetItems(batchCreateParam);

    // 构建有效数据项
    List<EvaluationSetItem> validItemDOS = new ArrayList<>();
    for (Entry<Long, Long> entry : batchResult.getIdMap().entrySet()) {
      int idx = entry.getKey().intValue();
      Long itemID = entry.getValue();
      itemDOS.get(idx).setItemId(itemID);
      validItemDOS.add(itemDOS.get(idx));
    }

    // 调用实验
    exptManager.invoke(InvokeExptReq.builder()
      .exptId(request.getExperimentId())
      .runId(request.getExperimentRunId())
      .spaceId(request.getWorkspaceId())
      .session(session)
      .items(validItemDOS)
      .ext(request.getExt())
      .build());

    // 更新实验轮次结果过滤器
    List<Long> itemIds = new ArrayList<>(batchResult.getIdMap().values());
    resultSvc.upsertExptTurnResultFilter(request.getWorkspaceId(), request.getExperimentId(), itemIds);

    return InvokeExperimentResponse.builder()
      .addedItems(batchResult.getIdMap())
      .errors(EvaluationSetItemApplicationConvertor.convertItemErrorGroupDO2DTOs(batchResult.getErrors()))
      .build();
  }

  @Override
  public FinishExperimentResponse finishExperiment(FinishExperimentRequest request) {
    Session session = Session.builder()
      .userId(String.valueOf(request.getSession().getUserId()))
      .build();

    Experiment got = exptManager.get(request.getExperimentId(), request.getWorkspaceId(), session);

    if (isExptFinished(got.getStatus())) {
      return FinishExperimentResponse.builder().build();
    }

    // 完成实验
    exptManager.finish(got, request.getExperimentRunId(), session);

    return FinishExperimentResponse.builder().build();
  }

  @Override
  public UpsertExptTurnResultFilterResponse upsertExptTurnResultFilter(UpsertExptTurnResultFilterRequest request) {
    if (Objects.equals(request.getFilterType(), UpsertExptTurnResultFilterTypeDTO.MANUAL.getValue())) {
      resultSvc.manualUpsertExptTurnResultFilter(
        request.getWorkspaceId(),
        request.getExperimentId(),
        request.getItemIds());
    }
    else if (Objects.equals(request.getFilterType(), UpsertExptTurnResultFilterTypeDTO.CHECK.getValue())) {
      resultSvc.compareExptTurnResultFilters(
        request.getWorkspaceId(),
        request.getExperimentId(),
        request.getItemIds(),
        request.getRetryTimes());
    }
    else {
      resultSvc.upsertExptTurnResultFilter(
        request.getWorkspaceId(),
        request.getExperimentId(),
        request.getItemIds());
    }

    return UpsertExptTurnResultFilterResponse.builder().build();
  }

  // 辅助方法
  private void buildExptTurnResultFilter(BatchGetExperimentResultRequest request, MGetExperimentResultParam param) {
    Map<Long, ExptTurnResultFilter> filters = new HashMap<>();
    param.setFilters(filters);
    if (request.getFilters() == null) {
      return;
    }
    if (Boolean.TRUE.equals(request.getUseAccelerator())) {
      Map<Long, ExptTurnResultFilterAccelerator> filterAccelerators = new HashMap<>();
      for (Entry<Long, ExperimentFilterDTO> entry : request.getFilters().entrySet()) {
        ExptTurnResultFilterAccelerator filter = ExperimentFilterApplicationConvertor.convertExptTurnResultFilterAccelerator(entry.getValue());
        filterAccelerators.put(entry.getKey(), filter);
      }
      param.setFilterAccelerators(filterAccelerators);
      param.setUseAccelerator(true);
    }
    else {

      for (Entry<Long, ExperimentFilterDTO> entry : request.getFilters().entrySet()) {
        ExptTurnResultFilter filter = ExperimentFilterApplicationConvertor.convertExptTurnResultFilter(entry.getValue().getFilters());
        filters.put(entry.getKey(), filter);
      }
      param.setUseAccelerator(false);
    }
  }

  private boolean hasDuplicates(List<Long> slice) {
    if (slice == null || slice.isEmpty()) {
      return false;
    }
    Set<Long> elementSet = new HashSet<>();
    for (Long value : slice) {
      if (elementSet.contains(value)) {
        return true;
      }
      elementSet.add(value);
    }
    return false;
  }

  private boolean isExptFinished(ExptStatus status) {
    return status == ExptStatus.SUCCESS ||
      status == ExptStatus.FAILED ||
      status == ExptStatus.TERMINATED || status == ExptStatus.SYSTEM_TERMINATED;
  }

  @Override
  public String createPdf(Long exptId, Long tenantId, OutputStream outputStream) {
    ExptResultData exptResultData = loadExperimentData(exptId, tenantId);
    experimentOpenPdfService.generatePdf(exptResultData, outputStream);
    return exptResultData.getExperiment().getName();
  }

  /**
   * 加载实验结果数据
   * */
  private ExptResultData loadExperimentData(Long exptId, Long tenantId) {
    ExptResultData exptResultData = new ExptResultData();
    List<Long> exptIds = Collections.singletonList(exptId);

    exptResultData.setExptId(exptId);
    exptResultData.setTenantId(tenantId);
    exptResultData.setExptIds(exptIds);

    // 加载实验基本信息
    BatchGetExperimentsResponse experimentsResponse = loadExpt(exptResultData);
    if (experimentsResponse.getExperiments().isEmpty()) {
      throw new BssException("实验不存在: " + exptId);
    }
    exptResultData.setExperiment(experimentsResponse.getExperiments().get(0));

    // 加载实验结果
    BatchGetExperimentResultResponse experimentResultResponse = loadExptResult(exptResultData);
    exptResultData.setExperimentResultResponse(experimentResultResponse);

    // 加载实验聚合结果
    BatchGetExperimentAggrResultResponse experimentAggrResultResponse = loadExptAggrResult(exptResultData);
    exptResultData.setExperimentAggrResultResponse(experimentAggrResultResponse);

    // 评估器
    Map<Long, String> evaluatorInfos = extractEvaluatorInfos(exptResultData);
    exptResultData.setEvaluatorInfos(evaluatorInfos);
    exptResultData.setEvaluatorInfoList(evaluatorInfos.keySet().stream().toList());

    // 评估器得分配置
    ExperimentDTO experiment = exptResultData.getExperiment();
    List<EvaluatorFieldMappingDTO> evaluatorFieldMappings = experiment.getEvaluatorFieldMapping();
    if (evaluatorFieldMappings == null) {
      evaluatorFieldMappings = Collections.emptyList();
    }
    Map<Long, Double> evaluatorPassScoreMap = new HashMap<>();
    for (EvaluatorFieldMappingDTO evaluatorFieldMapping : evaluatorFieldMappings) {
      evaluatorPassScoreMap.put(evaluatorFieldMapping.getEvaluatorVersionId(), evaluatorFieldMapping.getPassScore());
    }
    exptResultData.setEvaluatorPassScoreMap(evaluatorPassScoreMap);

    return exptResultData;
  }

  public BatchGetExperimentsResponse loadExpt(ExptResultData exptResultData) {
    BatchGetExperimentsRequest request = new BatchGetExperimentsRequest();
    request.setExptIds(exptResultData.getExptIds());
    request.setWorkspaceId(exptResultData.getTenantId());
    return this.batchGetExperiments(request);
  }

  public BatchGetExperimentResultResponse loadExptResult(ExptResultData exptResultData) {
    BatchGetExperimentResultRequest request = new BatchGetExperimentResultRequest();
    request.setBaselineExperimentId(exptResultData.getExptId());
    request.setExperimentIds(exptResultData.getExptIds());
    request.setWorkspaceId(exptResultData.getTenantId());
    request.setPageNumber(1);
    request.setPageSize(1000);
    return this.batchGetExperimentResult(request);
  }

  public BatchGetExperimentAggrResultResponse loadExptAggrResult(ExptResultData exptResultData) {
    BatchGetExperimentAggrResultRequest request = new BatchGetExperimentAggrResultRequest();
    request.setExperimentIds(exptResultData.getExptIds());
    request.setWorkspaceId(exptResultData.getTenantId());
    return this.batchGetExperimentAggrResult(request);
  }

  public Map<Long, String> extractEvaluatorInfos(ExptResultData exptResultData) {
    ExperimentDTO experiment = exptResultData.getExperiment();
    List<EvaluatorDTO> evaluators = experiment.getEvaluators();
    Map<Long, String> evaluatorInfos = Maps.newHashMap();
    if (evaluators == null) {
      return evaluatorInfos;
    }
    for (EvaluatorDTO evaluator : evaluators) {
      String name = evaluator.getName();
      EvaluatorVersionDTO currentVersion = evaluator.getCurrentVersion();
      if (currentVersion == null) {
        continue;
      }
      String version = currentVersion.getVersion();
      Long id = currentVersion.getId();
      evaluatorInfos.put(id, name + ":" + version);
    }
    return evaluatorInfos;
  }
}
