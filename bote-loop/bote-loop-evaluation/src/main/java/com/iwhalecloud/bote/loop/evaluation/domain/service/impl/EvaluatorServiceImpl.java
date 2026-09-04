package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorListResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorVersionListResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluatorRequest;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluatorVersionRequest;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.RunEvaluatorParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IEvaluatorRecordRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IEvaluatorRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorResponse;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorVersionResponse;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorSourceService;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 评估器服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/evaluator_impl.go
 * - 功能: 评估器业务逻辑实现
 * - 主要方法:
 * * listEvaluator - 分页查询评估器
 * * batchGetEvaluator - 批量获取评估器
 * * getEvaluator - 获取评估器
 * * createEvaluator - 创建评估器
 * * updateEvaluatorMeta - 更新评估器元信息
 * * updateEvaluatorDraft - 更新评估器草稿
 * * deleteEvaluator - 删除评估器
 * * runEvaluator - 运行评估器
 * * debugEvaluator - 调试评估器
 * * getEvaluatorVersion - 获取评估器版本
 * * batchGetEvaluatorVersion - 批量获取评估器版本
 * * listEvaluatorVersion - 分页查询评估器版本
 * * submitEvaluatorVersion - 提交评估器版本
 * * checkNameExist - 检查名称是否存在
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluatorServiceImpl结构体
 * - 使用Spring Service注解
 * - 依赖多个REPO和组件
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go sync.Once -> Java单例模式
 * - Go map[entity.EvaluatorType]EvaluatorSourceService -> Java Map<EvaluatorType, EvaluatorSourceService>
 */
@Service
public class EvaluatorServiceImpl implements EvaluatorService {
  /** 平台评估器的spaceId标识 */
  private static final Long PLATFORM_EVALUATOR_SPACE_ID = -1L;

  private final IIDGenerator idGenerator;
  private final IEvaluatorRepo evaluatorRepo;
  private final IEvaluatorRecordRepo evaluatorRecordRepo;
  private final Map<EvaluatorType, EvaluatorSourceService> evaluatorSourceServiceMap;

  public EvaluatorServiceImpl(IIDGenerator idGenerator, IEvaluatorRepo evaluatorRepo, IEvaluatorRecordRepo evaluatorRecordRepo,
                              List<EvaluatorSourceService> evaluatorSourceServices) {
    this.idGenerator = idGenerator;
    this.evaluatorRepo = evaluatorRepo;
    this.evaluatorRecordRepo = evaluatorRecordRepo;
    this.evaluatorSourceServiceMap = evaluatorSourceServices.stream()
      .collect(Collectors.toUnmodifiableMap(EvaluatorSourceService::evaluatorType, Function.identity()));
  }

  @Override
  public EvaluatorListResult listEvaluator(ListEvaluatorRequest request) {
    // 构建REPO层请求
    ListEvaluatorParam repoReq = buildListEvaluatorRequest(request);
    // 调用REPO层接口
    ListEvaluatorResponse result = evaluatorRepo.listEvaluator(repoReq);
    return EvaluatorListResult.builder().evaluators(result.getEvaluators()).total(result.getTotalCount()).build();
  }

  @Override
  public List<Evaluator> batchGetEvaluator(Long spaceId, List<Long> evaluatorIds, Boolean includeDeleted) {
    try {
      return evaluatorRepo.batchGetEvaluatorDraftByEvaluatorId(spaceId, evaluatorIds, includeDeleted);
    }
    catch (Exception e) {
      throw new BssException("批量获取评估器失败: " + e.getMessage(), e);
    }
  }

  @Override
  public Evaluator getEvaluator(Long spaceId, Long evaluatorId, Boolean includeDeleted) {
    try {
      if (evaluatorId == 0) {
        throw new BssException("评估器ID不能为空");
      }

      List<Evaluator> drafts = evaluatorRepo.batchGetEvaluatorDraftByEvaluatorId(spaceId, List.of(evaluatorId), includeDeleted);

      if (drafts.isEmpty()) {
        return null;
      }
      return drafts.getFirst();
    }
    catch (Exception e) {
      throw new BssException("获取评估器失败: " + e.getMessage(), e);
    }
  }

  @Override
  public Long createEvaluator(Evaluator evaluator, String cid) {
    try {
      // 参数校验
      validateCreateEvaluatorRequest(evaluator);
      // 注入用户信息
      injectUserInfo(evaluator);
      return evaluatorRepo.createEvaluator(evaluator);
    }
    catch (Exception e) {
      throw new BssException("创建评估器失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void updateEvaluatorMeta(Long id, Long spaceId, String name, String description, String userId, Long catalogItemId) {
    try {
      // 检查是否为平台评估器
      List<Evaluator> evaluators = evaluatorRepo.batchGetEvaluatorMetaById(List.of(id), false);
      if (!evaluators.isEmpty()) {
        Evaluator evaluator = evaluators.get(0);
        if (PLATFORM_EVALUATOR_SPACE_ID.equals(evaluator.getSpaceId())) {
          throw new BssException("平台评估器不允许修改");
        }
      }

      // 参数校验
      validateUpdateEvaluatorMetaRequest(id, spaceId, name);

      // 更新评估器元信息
      evaluatorRepo.updateEvaluatorMeta(id, name, description, userId, catalogItemId);
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BssException("更新评估器元信息失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void updateEvaluatorDraft(Evaluator versionDO) {
    try {
      // 检查是否为平台评估器
      if (versionDO == null) {
        throw new BssException("评估器为空!");
      }
      // 检查是否为平台评估器
      if (PLATFORM_EVALUATOR_SPACE_ID.equals(versionDO.getSpaceId())) {
        throw new BssException("平台评估器不允许修改");
      }
      versionDO.getBaseInfo().setUpdatedAt(System.currentTimeMillis());
      String userIDInContext = SessionContext.getCurrentUserId();
      versionDO.getBaseInfo().setUpdatedBy(UserInfo.builder()
        .userId(userIDInContext)
        .build());
      evaluatorRepo.updateEvaluatorDraft(versionDO);
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BssException("更新评估器草稿失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteEvaluator(List<Long> evaluatorIds, String userId) {
    try {
      // 检查是否包含平台评估器
      if (evaluatorIds != null && !evaluatorIds.isEmpty()) {
        List<Evaluator> evaluators = evaluatorRepo.batchGetEvaluatorDraftByEvaluatorId(null, evaluatorIds, false);
        for (Evaluator evaluator : evaluators) {
          if (PLATFORM_EVALUATOR_SPACE_ID.equals(evaluator.getSpaceId())) {
            throw new BssException("平台评估器不允许删除");
          }
        }
      }

      evaluatorRepo.batchDeleteEvaluator(evaluatorIds, userId);
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BssException("删除评估器失败: " + e.getMessage(), e);
    }
  }

  @Override
  public EvaluatorRecord runEvaluator(RunEvaluatorParam request) {
    try {
      // 获取评估器版本
      List<Evaluator> evaluatorDOList = evaluatorRepo.batchGetEvaluatorByVersionId(
        request.getSpaceId(), List.of(request.getEvaluatorVersionId()), false);
      if (evaluatorDOList.isEmpty()) {
        throw new BssException("评估器版本不存在");
      }
      Evaluator evaluatorDO = evaluatorDOList.getFirst();
      // 获取评估器源服务
      EvaluatorSourceService evaluatorSourceService = evaluatorSourceServiceMap.get(evaluatorDO.getEvaluatorType());
      if (evaluatorSourceService == null) {
        throw new BssException("评估器不存在");
      }
      // 预处理
      evaluatorSourceService.preHandle(evaluatorDO);
      // 运行评估器
      EvaluatorRunResult evaluatorRunResult = evaluatorSourceService.run(request.getSpaceId(), evaluatorDO, request.getInputData());
      // 生成记录ID
      Long recordId = idGenerator.genId();
      String userIDInContext = SessionContext.getCurrentUserId();
      String logId = getCurrentLogId();
      // 创建评估器记录
      EvaluatorRecord recordDO = EvaluatorRecord.builder()
        .id(recordId)
        .spaceId(request.getSpaceId())
        .experimentId(request.getExperimentId())
        .experimentRunId(request.getExperimentRunId())
        .itemId(request.getItemId())
        .turnId(request.getTurnId())
        .evaluatorVersionId(request.getEvaluatorVersionId())
        .traceId(evaluatorRunResult.getTraceId())
        .logId(logId)
        .evaluatorInputData(request.getInputData())
        .evaluatorOutputData(evaluatorRunResult.getOutput())
        .status(evaluatorRunResult.getRunStatus())
        .ext(request.getExt())
        .baseInfo(BaseInfo.builder()
          .createdBy(UserInfo.builder()
            .userId(userIDInContext)
            .build())
          .build())
        .build();
      evaluatorRecordRepo.createEvaluatorRecord(recordDO);
      return recordDO;
    }
    catch (Exception e) {
      throw new BssException("运行评估器失败: " + e.getMessage(), e);
    }
  }

  @Override
  public EvaluatorOutputData debugEvaluator(Long workspaceId, Evaluator evaluatorDO, EvaluatorInputData inputData) {
    try {
      if (evaluatorDO == null || evaluatorDO.getEvaluatorVersion() == null) {
        throw new BssException("评估器不存在");
      }
      EvaluatorSourceService evaluatorSourceService = evaluatorSourceServiceMap.get(evaluatorDO.getEvaluatorType());
      if (evaluatorSourceService == null) {
        throw new BssException("评估器不存在");
      }
      // 预处理
      evaluatorSourceService.preHandle(evaluatorDO);
      // 调试评估器
      return evaluatorSourceService.debug(workspaceId, evaluatorDO, inputData);
    }
    catch (Exception e) {
      throw new BssException("调试评估器失败: " + e.getMessage(), e);
    }
  }

  @Override
  public Evaluator getEvaluatorVersion(Long evaluatorVersionId, Boolean includeDeleted) {
    try {
      List<Evaluator> evaluatorDOList = evaluatorRepo.batchGetEvaluatorByVersionId(
        null, List.of(evaluatorVersionId), includeDeleted);

      if (evaluatorDOList.isEmpty()) {
        return null;
      }

      return evaluatorDOList.getFirst();
    }
    catch (Exception e) {
      throw new BssException("获取评估器版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Evaluator> batchGetEvaluatorVersion(Long spaceId, List<Long> evaluatorVersionIds, Boolean includeDeleted) {
    try {
      return evaluatorRepo.batchGetEvaluatorByVersionId(spaceId, evaluatorVersionIds, includeDeleted);
    }
    catch (Exception e) {
      throw new BssException("批量获取评估器版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public EvaluatorVersionListResult listEvaluatorVersion(ListEvaluatorVersionRequest request) {
    try {
      // 构建REPO层请求
      ListEvaluatorVersionParam repoReq = buildListEvaluatorVersionRequest(request);

      // 调用REPO层接口
      ListEvaluatorVersionResponse result = evaluatorRepo.listEvaluatorVersion(repoReq);

      return EvaluatorVersionListResult.builder()
        .evaluatorVersions(result.getVersions())
        .total(result.getTotalCount())
        .build();
    }
    catch (Exception e) {
      throw new BssException("分页查询评估器版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public Evaluator submitEvaluatorVersion(Evaluator evaluatorDO, String version, String description, String cid) {
    try {
      if (evaluatorDO == null) {
        throw new BssException("评估器为空,无法提交版本");
      }
      // 检查是否为平台评估器
      if (PLATFORM_EVALUATOR_SPACE_ID.equals(evaluatorDO.getSpaceId())) {
        throw new BssException("平台评估器不允许修改");
      }
      // 生成版本ID
      Long versionId = idGenerator.genId();
      String userIDInContext = SessionContext.getCurrentUserId();
      // 检查版本是否存在
      boolean versionExist = evaluatorRepo.checkVersionExist(evaluatorDO.getId(), version);
      if (versionExist) {
        throw new BssException("版本已存在");
      }
      // 设置版本信息
      evaluatorDO.getEvaluatorVersion().setId(versionId);
      evaluatorDO.getEvaluatorVersion().setVersion(version);
      evaluatorDO.getEvaluatorVersion().setDescription(description);
      // 设置基础信息
      evaluatorDO.setBaseInfo(BaseInfo.builder()
        .updatedBy(UserInfo.builder()
          .userId(userIDInContext)
          .build())
        .updatedAt(System.currentTimeMillis())
        .build());
      evaluatorDO.getEvaluatorVersion().setBaseInfo(BaseInfo.builder()
        .createdBy(UserInfo.builder()
          .userId(userIDInContext)
          .build())
        .updatedBy(UserInfo.builder()
          .userId(userIDInContext)
          .build())
        .updatedAt(System.currentTimeMillis())
        .createdAt(System.currentTimeMillis())
        .build());
      evaluatorDO.setLatestVersion(version);
      evaluatorDO.setDraftSubmitted(true);
      // 提交版本
      evaluatorRepo.submitEvaluatorVersion(evaluatorDO);
      return evaluatorDO;
    }
    catch (Exception e) {
      throw new BssException("提交评估器版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public Boolean checkNameExist(Long spaceId, Long evaluatorId, String name) {
    try {
      return evaluatorRepo.checkNameExist(spaceId, evaluatorId, name);
    }
    catch (Exception e) {
      throw new BssException("检查名称是否存在失败: " + e.getMessage(), e);
    }
  }

  public static final Long EVALUATOR_EMPTY_ID = -1L;

  private void validateCreateEvaluatorRequest(Evaluator evaluator) {
    if (evaluator == null) {
      throw new BssException("评估器不能为空");
    }
    if (evaluator.getSpaceId() == null) {
      throw new BssException("空间ID不能为空");
    }
    if (evaluator.getName() != null && !evaluator.getName().isEmpty()) {
      boolean exist = evaluatorRepo.checkNameExist(evaluator.getSpaceId(), EVALUATOR_EMPTY_ID, evaluator.getName());
      if (exist) {
        throw new BssException("评估器名称已存在");
      }
    }
  }

  private void validateUpdateEvaluatorMetaRequest(Long id, Long spaceId, String name) {
    if (name != null && !name.isEmpty()) {
      boolean exist = evaluatorRepo.checkNameExist(spaceId, id, name);
      if (exist) {
        throw new BssException("评估器名称已存在");
      }
    }
  }

  private void injectUserInfo(Evaluator evaluatorDO) {
    String userIDInContext = SessionContext.getCurrentUserId();
    evaluatorDO.setBaseInfo(BaseInfo.builder()
      .createdBy(UserInfo.builder()
        .userId(userIDInContext)
        .build())
      .updatedBy(UserInfo.builder()
        .userId(userIDInContext)
        .build())
      .createdAt(System.currentTimeMillis())
      .updatedAt(System.currentTimeMillis())
      .build());

    evaluatorDO.getEvaluatorVersion().setBaseInfo(BaseInfo.builder()
      .createdBy(UserInfo.builder()
        .userId(userIDInContext)
        .build())
      .updatedBy(UserInfo.builder()
        .userId(userIDInContext)
        .build())
      .createdAt(System.currentTimeMillis())
      .updatedAt(System.currentTimeMillis())
      .build());
  }

  private ListEvaluatorParam buildListEvaluatorRequest(ListEvaluatorRequest request) {
    String catalogItemId = request.getCatalogItemId();

    ListEvaluatorParam req = new ListEvaluatorParam();
    req.setSpaceId(request.getSpaceId());
    req.setSearchName(request.getSearchName());
    req.setCreatorIds(request.getCreatorIds());
    req.setPageSize(request.getPageSize());
    req.setPageNum(request.getPageNum());
    req.setEvaluatorType(request.getEvaluatorType());
    req.setWithVersion(request.getWithVersion() != null && request.getWithVersion());
    req.setCatalogItemId(catalogItemId == null ? null : Long.parseLong(catalogItemId));

    // 默认排序
    if (request.getOrderBys() == null || request.getOrderBys().isEmpty()) {
      req.setOrderBy(List.of(new OrderBy("updated_at", false)));
    }
    else {
      req.setOrderBy(request.getOrderBys());
    }

    return req;
  }

  private ListEvaluatorVersionParam buildListEvaluatorVersionRequest(ListEvaluatorVersionRequest request) {
    ListEvaluatorVersionParam req = new ListEvaluatorVersionParam();
    req.setEvaluatorId(request.getEvaluatorId());
    req.setQueryVersions(request.getQueryVersions());
    req.setPageSize(request.getPageSize());
    req.setPageNum(request.getPageNum());

    // 默认排序
    if (request.getOrderBys() == null || request.getOrderBys().isEmpty()) {
      req.setOrderBy(List.of(new OrderBy("updated_at", false)));
    }
    else {
      req.setOrderBy(request.getOrderBys());
    }

    return req;
  }

  private String getCurrentLogId() {
    // 从上下文获取日志ID
    return "current_log_id";
  }
}
