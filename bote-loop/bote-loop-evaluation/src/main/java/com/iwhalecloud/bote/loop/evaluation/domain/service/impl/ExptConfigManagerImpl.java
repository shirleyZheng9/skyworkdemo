package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateExptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetCreateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggregateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorVersionRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTuple;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTupleID;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterKeyMapping;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldTypeMapping;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.GetExptTupleOption;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListExptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.VersionedEvalSetID;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.VersionedTargetID;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExperimentRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetVersionService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptAggrResultService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptResultService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IEvalTargetService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IExptConfigManager;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 实验管理服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/expt_manage_impl.go
 * - 功能: 实验管理业务逻辑实现
 * - 主要方法:
 * * mGetDetail - 批量获取实验详情
 * * getDetail - 获取实验详情
 * * checkName - 检查实验名称
 * * mDelete - 批量删除实验
 * * createExpt - 创建实验
 * * create - 创建实验
 * * get - 获取实验
 * * mGet - 批量获取实验
 * * list - 分页查询实验列表
 * * update - 更新实验
 * * delete - 删除实验
 * * clone - 克隆实验
 * <p>
 * Java实现说明:
 * - 对应Go的ExptMangerImpl结构体
 * - 使用Spring Service注解
 * - 依赖多个REPO和组件
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go sync.Once -> Java单例模式
 * - Go events.ExptEventPublisher -> Java ExptEventPublisher
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptConfigManagerImpl implements IExptConfigManager {
  private static final Logger logger = LoggerFactory.getLogger(ExptConfigManagerImpl.class);
  private static final String DEFAULT_SOURCE_TARGET_VERSION = "0.0.1";
  private final ExptResultService exptResultService;
  private final ExptAggrResultService exptAggrResultService;
  private final IExperimentRepo exptRepo;
  private final IIDGenerator idgenerator;
  private final EvaluationSetVersionService evaluationSetVersionService;
  private final EvaluationSetService evaluationSetService;
  private final IEvalTargetService evalTargetService;
  private final EvaluatorService evaluatorService;

  @Override
  public List<Experiment> mGetDetail(List<Long> exptIds, Long spaceId, Session session) {
    List<Experiment> exptBasics = mGet(exptIds, spaceId);
    List<Experiment> exptDetails = packExperimentResult(exptBasics, spaceId, session);
    List<ExptTuple> exptTuples = mGetTupleByExpt(exptDetails, spaceId);
    for (int i = 0; i < exptTuples.size(); i++) {
      exptDetails.get(i).setEvalSet(exptTuples.get(i).getEvalSet());
      exptDetails.get(i).setTarget(exptTuples.get(i).getTarget());
      exptDetails.get(i).setEvaluators(exptTuples.get(i).getEvaluators());
    }
    return exptDetails;
  }

  @Override
  public Experiment getDetail(Long exptId, Long spaceId, Session session, GetExptTupleOption... opts) {
    Experiment expt = get(exptId, spaceId, session);
    ExptTuple tuple = getTupleByExpt(expt, spaceId);
    expt.setEvaluators(tuple.getEvaluators());
    expt.setEvalSet(tuple.getEvalSet());
    expt.setTarget(tuple.getTarget());
    List<Experiment> expts = packExperimentResult(List.of(expt), spaceId, session);
    return expts.getFirst();
  }

  @Override
  public Boolean checkName(String name, Long spaceId, Session session) {
    Experiment expt = exptRepo.getByName(name, spaceId);
    return expt == null;
  }

  @Override
  public void mDelete(List<Long> exptIds, Long spaceId, Session session) {
    exptRepo.mDelete(exptIds, spaceId);
  }

  @Override
  public Experiment createExpt(CreateExptParam req, Session session) {
    if (req.getExptType() == ExptType.ONLINE) {
      req.getCreateEvalTargetParam().setSourceTargetVersion(DEFAULT_SOURCE_TARGET_VERSION);
    }
    EvalTargetCreateResult targetCreateResult = evalTargetService.createEvalTarget(
      req.getWorkspaceId(),
      req.getCreateEvalTargetParam().getSourceTargetId(),
      req.getCreateEvalTargetParam().getSourceTargetVersion(),
      req.getCreateEvalTargetParam().getEvalTargetType(),
      req.getCreateEvalTargetParam().getBotPublishVersion(),
      req.getCreateEvalTargetParam().getBotInfoType()
    );
    ExptTuple tuple = getExptTupleByID(ExptTupleID.builder()
      .versionedEvalSetId(VersionedEvalSetID.builder()
        .evalSetId(req.getEvalSetId())
        .versionId(req.getEvalSetVersionId())
        .build())
      .versionedTargetId(VersionedTargetID.builder()
        .targetId(targetCreateResult.getId())
        .versionId(targetCreateResult.getVersionId())
        .build())
      .evaluatorVersionIds(req.getEvaluatorVersionIds())
      .build(), req.getWorkspaceId());
    int evaluatorCount = tuple.getEvaluators().size();
    List<Long> ids = idgenerator.genMultiIds(2 + evaluatorCount);
    EvaluatorRefsAndMappings refsAndMappings = buildEvaluatorRefsAndMappings(
      tuple.getEvaluators(), ids, req.getWorkspaceId());
    Long evalSetVersionId = determineEvalSetVersionId(req, tuple);
    Experiment experiment = buildExperiment(req, targetCreateResult, tuple, ids.getFirst(), evalSetVersionId, refsAndMappings.evaluatorRefs());
    updateTargetVersionIdInConf(experiment, targetCreateResult.getVersionId());
    Date now = new Date();
    ExptStats stats = ExptStats.builder()
      .id(ids.get(1))
      .spaceId(req.getWorkspaceId())
      .exptId(experiment.getId())
      .createdAt(now)
      .updatedAt(now)
      .build();
    exptResultService.createStats(stats);
    exptResultService.insertExptTurnResultFilterKeyMappings(refsAndMappings.mappings());
    create(experiment);
    return experiment;
  }

  /**
   * 构建评估器引用和映射
   */
  private EvaluatorRefsAndMappings buildEvaluatorRefsAndMappings(
    List<Evaluator> evaluators, List<Long> ids, Long workspaceId) {
    List<ExptEvaluatorVersionRef> evaluatorRefs = new ArrayList<>();
    List<ExptTurnResultFilterKeyMapping> mappings = new ArrayList<>();
    for (int i = 0; i < evaluators.size(); i++) {
      Evaluator evaluator = evaluators.get(i);
      evaluatorRefs.add(ExptEvaluatorVersionRef.builder()
        .evaluatorId(evaluator.getId())
        .evaluatorVersionId(evaluator.getEvaluatorVersion().getId())
        .build());
      mappings.add(ExptTurnResultFilterKeyMapping.builder()
        .id(ids.get(2 + i))
        .spaceId(workspaceId)
        .exptId(ids.getFirst())
        .fromField(String.valueOf(evaluator.getEvaluatorVersion().getId()))
        .toKey("key" + (i + 1))
        .fieldType(FieldTypeMapping.EVALUATOR)
        .build());
    }
    return new EvaluatorRefsAndMappings(evaluatorRefs, mappings);
  }

  /**
   * 确定评估集版本ID
   */
  private Long determineEvalSetVersionId(CreateExptParam req, ExptTuple tuple) {
    Long evalSetVersionId = req.getEvalSetVersionId();
    if (evalSetVersionId == null && tuple.getEvalSet() != null) {
      if (tuple.getEvalSet().getEvaluationSetVersion() != null) {
        evalSetVersionId = tuple.getEvalSet().getEvaluationSetVersion().getId();
      }
      else if (tuple.getEvalSet().getId() != null) {
        evalSetVersionId = tuple.getEvalSet().getId();
      }
    }
    if (evalSetVersionId == null && req.getEvalSetId() != null) {
      evalSetVersionId = req.getEvalSetId();
    }
    if (evalSetVersionId == null) {
      throw new BssException("evalSetVersionId 不能为 null，请提供 evalSetVersionId 或 evalSetId");
    }
    return evalSetVersionId;
  }

  /**
   * 构建实验对象
   */
  private Experiment buildExperiment(CreateExptParam req,
                                     EvalTargetCreateResult targetCreateResult, ExptTuple tuple,
                                     Long experimentId, Long evalSetVersionId, List<ExptEvaluatorVersionRef> evaluatorRefs) {
    return Experiment.builder()
      .id(experimentId)
      .spaceId(req.getWorkspaceId())
      .createdBy(SessionContext.getCurrentUserId())
      .name(req.getName())
      .description(req.getDesc())
      .evalSetVersionId(evalSetVersionId)
      .evalSetId(req.getEvalSetId())
      .targetVersionId(targetCreateResult.getVersionId())
      .targetType(req.getCreateEvalTargetParam().getEvalTargetType())
      .targetId(targetCreateResult.getId())
      .evaluatorVersionRef(evaluatorRefs)
      .evalConf(req.getExptConf())
      .status(ExptStatus.PENDING)
      .startAt(new Date())
      .exptType(req.getExptType())
      .maxAliveTime(req.getMaxAliveTime())
      .sourceType(req.getSourceType())
      .sourceId(req.getSourceId())
      .catalogItemId(req.getCatalogItemId())
      .target(tuple.getTarget())
      .evaluators(tuple.getEvaluators())
      .evalSet(tuple.getEvalSet())
      .build();
  }

  /**
   * 更新配置中的目标版本ID
   */
  private void updateTargetVersionIdInConf(Experiment experiment, Long targetVersionId) {
    if (experiment.getEvalConf() != null &&
      experiment.getEvalConf().getConnectorConf() != null &&
      experiment.getEvalConf().getConnectorConf().getTargetConf() != null) {
      experiment.getEvalConf().getConnectorConf().getTargetConf().setTargetVersionId(targetVersionId);
    }
  }

  public void create(Experiment expt) {
    List<ExptEvaluatorRef> refs = expt.toEvaluatorRefDO();
    Experiment existExpt = exptRepo.getByName(expt.getName(), expt.getSpaceId());
    Assert.isNull(existExpt, "实验名称已存在: " + expt.getName());
    exptRepo.create(expt, refs);
  }

  @Override
  public Experiment get(Long exptId, Long spaceId, Session session) {
    List<Experiment> expts = mGet(List.of(exptId), spaceId);
    if (expts.isEmpty()) {
      throw new BssException("实验不存在: " + exptId);
    }
    return expts.getFirst();
  }

  @Override
  public List<Experiment> mGet(List<Long> exptIds, Long spaceId) {
    return exptRepo.mGetById(exptIds, spaceId);
  }

  @Override
  public PageInfo<Experiment> list(ListExptParam param) {
    Integer pageNumber = param.getPageNumber();
    Integer pageSize = param.getPageSize();
    ExptListFilter filter = param.getFilter();
    List<OrderBy> orderBys = param.getOrders();
    Long spaceId = param.getSpaceId();
    Long catalogItemId = param.getCatalogItemId();
    PageInfo<Experiment> expts = exptRepo.list(pageNumber, pageSize, filter, orderBys, spaceId, catalogItemId);
    List<ExptTupleID> tupleIds = new ArrayList<>();
    for (Experiment exptDO : expts.getList()) {
      tupleIds.add(packTupleID(exptDO));
    }
    List<ExptTuple> exptTuples = mGetTupleByID(tupleIds, spaceId);
    for (int i = 0; i < exptTuples.size(); i++) {
      expts.getList().get(i).setEvalSet(exptTuples.get(i).getEvalSet());
      expts.getList().get(i).setTarget(exptTuples.get(i).getTarget());
      expts.getList().get(i).setEvaluators(exptTuples.get(i).getEvaluators());
    }
    packExperimentResult(expts.getList(), spaceId, null);
    return expts;
  }

  @Override
  public PageInfo<Experiment> listExptRaw(Integer pageNumber, Integer pageSize, Long spaceId, ExptListFilter filter) {
    return exptRepo.list(pageNumber, pageSize, filter, null, spaceId);
  }

  @Override
  public void update(Experiment expt, Session session) {
    exptRepo.update(expt);
  }

  @Override
  public void delete(Long exptId, Long spaceId, Session session) {
    exptRepo.delete(exptId, spaceId);
  }

  @Override
  public Experiment clone(Long exptId, Long spaceId) {
    Experiment expt = exptRepo.getById(exptId, spaceId);
    Long id = idgenerator.genId();
    expt.setId(id);
    create(expt);
    return expt;
  }

  // 私有辅助方法
  private List<Experiment> packExperimentResult(List<Experiment> expts, Long spaceId, Session session) {
    if (expts.isEmpty()) {
      return expts;
    }
    List<Long> exptIds = expts.stream().map(Experiment::getId).collect(Collectors.toList());
    List<ExptStats> stats = exptResultService.mGetStats(exptIds, spaceId, session);
    Map<Long, ExptStats> exptId2Stats = stats.stream()
      .collect(Collectors.toMap(ExptStats::getExptId, s -> s));
    for (Experiment expt : expts) {
      expt.setStats(exptId2Stats.get(expt.getId()));
    }
    try {
      List<ExptAggregateResult> aggrResults = exptAggrResultService.batchGetExptAggrResultByExperimentIds(spaceId, exptIds);
      Map<Long, ExptAggregateResult> arMemo = aggrResults.stream()
        .collect(Collectors.toMap(ExptAggregateResult::getExperimentId, r -> r));
      for (Experiment expt : expts) {
        expt.setAggregateResult(arMemo.get(expt.getId()));
      }
    }
    catch (Exception e) {
      // 记录日志但不影响主流程
      logger.error("BatchGetExptAggrResultByExperimentIDs fail, expt_ids: {}, err: {}", exptIds, e.getMessage(), e);
    }
    return expts;
  }

  private ExptTuple getTupleByExpt(Experiment expt, Long spaceId) {
    return getExptTupleByID(packTupleID(expt), spaceId);
  }

  private List<ExptTuple> mGetTupleByExpt(List<Experiment> expts, Long spaceId) {
    List<ExptTupleID> tupleIds = new ArrayList<>();
    for (Experiment exptDO : expts) {
      tupleIds.add(packTupleID(exptDO));
    }
    return mGetTupleByID(tupleIds, spaceId);
  }

  private ExptTupleID packTupleID(Experiment expt) {
    List<Long> evaluatorVersionIds = expt.getEvaluatorVersionRef().stream()
      .map(ExptEvaluatorVersionRef::getEvaluatorVersionId)
      .collect(Collectors.toList());
    return ExptTupleID.builder()
      .versionedTargetId(VersionedTargetID.builder()
        .targetId(expt.getTargetId())
        .versionId(expt.getTargetVersionId())
        .build())
      .versionedEvalSetId(VersionedEvalSetID.builder()
        .evalSetId(expt.getEvalSetId())
        .versionId(expt.getEvalSetVersionId())
        .build())
      .evaluatorVersionIds(evaluatorVersionIds)
      .build();
  }

  private ExptTuple getExptTupleByID(ExptTupleID exptTupleId, Long spaceId) {
    EvalTarget target = null;
    EvaluationSet evalSet = null;
    List<Evaluator> evaluators = new ArrayList<>();
    if (exptTupleId.getVersionedTargetId() != null) {
      target = evalTargetService.getEvalTargetVersion(spaceId, exptTupleId.getVersionedTargetId().getVersionId(), true);
    }
    if (exptTupleId.getVersionedEvalSetId() != null) {
      Long evalSetId = exptTupleId.getVersionedEvalSetId().getEvalSetId();
      Long versionId = exptTupleId.getVersionedEvalSetId().getVersionId();
      if (evalSetId != null && versionId != null && !evalSetId.equals(versionId)) {
        EvaluationSetVersionResult result = evaluationSetVersionService.getEvaluationSetVersion(spaceId, versionId, true);
        if (result.getEvaluationSet() != null) {
          evalSet = result.getEvaluationSet();
          evalSet.setEvaluationSetVersion(result.getVersion());
        }

      }
      else if (evalSetId != null) {
        evalSet = evaluationSetService.getEvaluationSet(spaceId, evalSetId, false);
      }
    }
    if (!exptTupleId.getEvaluatorVersionIds().isEmpty()) {
      evaluators = evaluatorService.batchGetEvaluatorVersion(spaceId, exptTupleId.getEvaluatorVersionIds(), true);
    }
    return ExptTuple.builder()
      .target(target)
      .evalSet(evalSet)
      .evaluators(evaluators)
      .build();
  }

  private List<ExptTuple> mGetTupleByID(List<ExptTupleID> tupleIds, Long spaceId) {
    // 实现批量获取逻辑
    List<ExptTuple> result = new ArrayList<>();
    for (ExptTupleID tupleId : tupleIds) {
      result.add(getExptTupleByID(tupleId, spaceId));
    }
    return result;
  }

  /**
   * 评估器引用和映射的容器类
   */
  private record EvaluatorRefsAndMappings(List<ExptEvaluatorVersionRef> evaluatorRefs, List<ExptTurnResultFilterKeyMapping> mappings) {
  }
}
