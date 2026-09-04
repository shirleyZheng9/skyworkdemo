package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvalTargetBySourceReqParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BotInfoType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetCreateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetExecuteResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetUsage;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExecuteEvalTargetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExecuteTargetCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IEvalTargetRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.BatchGetEvalTargetBySourceParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.CreateEvalTargetResult;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IEvalTargetService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ISourceEvalTargetOperateService;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 评测对象服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/target_impl.go
 * - 功能: 评测对象业务逻辑实现
 * - 主要方法:
 * * createEvalTarget - 创建评测对象
 * * getEvalTarget - 获取评测对象
 * * getEvalTargetVersion - 获取评测对象版本
 * * batchGetEvalTargetBySource - 根据源对象批量获取评测对象
 * * batchGetEvalTargetVersion - 批量获取评测对象版本
 * * executeTarget - 执行评测对象
 * * getRecordById - 根据ID获取评测记录
 * * batchGetRecordByIds - 根据ID列表批量获取评测记录
 * <p>
 * Java实现说明:
 * - 对应Go的EvalTargetServiceImpl结构体
 * - 使用Spring Service注解
 * - 依赖多个REPO和组件
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go map[entity.EvalTargetType]ISourceEvalTargetOperateService -> Java Map<EvalTargetType, ISourceEvalTargetOperateService>
 */
@Service
public class EvalTargetServiceImpl implements IEvalTargetService {
  private static final Logger logger = LoggerFactory.getLogger(EvalTargetServiceImpl.class);

  private final IIDGenerator idGenerator;
  private final IEvalTargetRepo evalTargetRepo;
  private final Map<EvalTargetType, ISourceEvalTargetOperateService> typedOperators;

  public EvalTargetServiceImpl(IIDGenerator idGenerator, IEvalTargetRepo evalTargetRepo, List<ISourceEvalTargetOperateService> sourceEvalTargetOperateServices) {
    this.idGenerator = idGenerator;
    this.evalTargetRepo = evalTargetRepo;
    this.typedOperators = sourceEvalTargetOperateServices.stream()
      .collect(Collectors.toUnmodifiableMap(ISourceEvalTargetOperateService::evalType, Function.identity()));
  }

  @Override
  public EvalTargetCreateResult createEvalTarget(Long spaceId, String sourceTargetId,
                                                 String sourceTargetVersion,
                                                 EvalTargetType targetType, String botPublishVersion, BotInfoType botInfoType) {
    if (spaceId == null) {
      throw new BssException("空间ID不能为空");
    }
    if (sourceTargetId == null || sourceTargetId.isEmpty()) {
      throw new BssException("源对象ID不能为空");
    }
    if (sourceTargetVersion == null || sourceTargetVersion.isEmpty()) {
      throw new BssException("源对象版本不能为空");
    }
    if (targetType == null) {
      throw new BssException("评测对象类型不能为空");
    }

    ISourceEvalTargetOperateService operator = typedOperators.get(targetType);
    if (operator == null) {
      throw new BssException("不支持的评测对象类型: " + targetType);
    }

    EvalTarget evalTarget = operator.buildBySource(spaceId, sourceTargetId, sourceTargetVersion);
    if (evalTarget == null) {
      throw new BssException("根据源对象构建评测对象失败");
    }

    CreateEvalTargetResult savedTarget = evalTargetRepo.createEvalTarget(evalTarget);
    return new EvalTargetCreateResult(savedTarget.getId(), savedTarget.getVersionId());
  }

  @Override
  public EvalTarget getEvalTarget(Long targetId) {
    if (targetId == null || targetId <= 0) {
      throw new BssException("评测对象ID不能为空");
    }

    return evalTargetRepo.getEvalTarget(targetId);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public EvalTarget getEvalTargetVersion(Long spaceId, Long versionId, Boolean needSourceInfo) {
    if (spaceId == null) {
      throw new BssException("空间ID不能为空");
    }
    if (versionId == null || versionId <= 0) {
      throw new BssException("版本ID不能为空");
    }

    EvalTarget target = evalTargetRepo.getEvalTargetVersion(spaceId, versionId);

    // 如果需要源信息，则包装源信息
    if (needSourceInfo) {
      for (ISourceEvalTargetOperateService operator : typedOperators.values()) {
        try {
          operator.packSourceVersionInfo(spaceId, List.of(target));
        }
        catch (Exception e) {
          // 记录警告但不中断流程
          logger.warn("包装源版本信息失败: {}", e.getMessage(), e);
        }
      }
    }

    return target;
  }

  @Override
  public List<EvalTarget> batchGetEvalTargetBySource(BatchGetEvalTargetBySourceReqParam param) {
    if (param == null) {
      throw new BssException("批量获取参数不能为空");
    }
    if (param.getSpaceId() == null) {
      throw new BssException("空间ID不能为空");
    }
    if (param.getSourceTargetId() == null || param.getSourceTargetId().isEmpty()) {
      throw new BssException("源对象ID列表不能为空");
    }
    if (param.getTargetType() == null) {
      throw new BssException("评测对象类型不能为空");
    }

    BatchGetEvalTargetBySourceParam repoParam = new BatchGetEvalTargetBySourceParam();
    repoParam.setSpaceId(param.getSpaceId());
    repoParam.setTargetType(param.getTargetType());
    repoParam.setSourceTargetId(param.getSourceTargetId());

    return evalTargetRepo.batchGetEvalTargetBySource(repoParam);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public List<EvalTarget> batchGetEvalTargetVersion(Long spaceId, List<Long> versionIds, Boolean needSourceInfo) {
    if (spaceId == null) {
      throw new BssException("空间ID不能为空");
    }
    if (versionIds == null || versionIds.isEmpty()) {
      throw new BssException("版本ID列表不能为空");
    }

    List<EvalTarget> targets = evalTargetRepo.batchGetEvalTargetVersion(spaceId, versionIds);

    // 如果需要源信息，则包装源信息
    if (needSourceInfo) {
      for (ISourceEvalTargetOperateService operator : typedOperators.values()) {
        try {
          operator.packSourceVersionInfo(spaceId, targets);
        }
        catch (Exception e) {
          // 记录警告但不中断流程
          logger.warn("包装源版本信息失败: {}", e.getMessage(), e);
        }
      }
    }

    return targets;
  }

  @Override
  public EvalTargetRecord executeTarget(Long spaceId, Long targetId, Long targetVersionId,
                                        ExecuteTargetCtx param, EvalTargetInputData inputData) {
    validateExecuteTargetParameters(spaceId, targetId, targetVersionId, inputData, param);
    EvalTarget target = getAndValidateTarget(spaceId, targetVersionId);
    ISourceEvalTargetOperateService operator = getOperatorForTarget(target);
    validateInputData(operator, spaceId, target, inputData);

    ExecuteEvalTargetParam executeParam = buildExecuteParam(targetId, targetVersionId, target, inputData);
    EvalTargetExecuteResult result = executeTargetWithErrorHandling(operator, spaceId, executeParam);

    return createAndSaveRecord(spaceId, targetId, targetVersionId, param, inputData, result);
  }

  private void validateExecuteTargetParameters(Long spaceId, Long targetId, Long targetVersionId,
      EvalTargetInputData inputData, ExecuteTargetCtx param) {
    if (spaceId == null) {
      throw new BssException("空间ID不能为空");
    }
    if (targetId == null || targetId <= 0) {
      throw new BssException("评测对象ID不能为空");
    }
    if (targetVersionId == null || targetVersionId <= 0) {
      throw new BssException("评测对象版本ID不能为空");
    }
    if (inputData == null) {
      throw new BssException("输入数据不能为空");
    }
    if (param == null) {
      throw new BssException("执行上下文不能为空");
    }
  }

  private EvalTarget getAndValidateTarget(Long spaceId, Long targetVersionId) {
    EvalTarget target = getEvalTargetVersion(spaceId, targetVersionId, false);
    if (target == null) {
      throw new BssException("评测对象版本不存在");
    }
    return target;
  }

  private ISourceEvalTargetOperateService getOperatorForTarget(EvalTarget target) {
    ISourceEvalTargetOperateService operator = typedOperators.get(target.getEvalTargetType());
    if (operator == null) {
      throw new BssException("不支持的评测对象类型: " + target.getEvalTargetType());
    }
    return operator;
  }

  private void validateInputData(ISourceEvalTargetOperateService operator, Long spaceId,
      EvalTarget target, EvalTargetInputData inputData) {
    operator.validateInput(spaceId, target.getEvalTargetVersion().getInputSchema(), inputData);
  }

  private ExecuteEvalTargetParam buildExecuteParam(Long targetId, Long targetVersionId,
      EvalTarget target, EvalTargetInputData inputData) {
    ExecuteEvalTargetParam executeParam = new ExecuteEvalTargetParam();
    executeParam.setTargetId(targetId);
    executeParam.setVersionId(targetVersionId);
    executeParam.setSourceTargetId(target.getSourceTargetId());
    executeParam.setSourceTargetVersion(target.getEvalTargetVersion().getSourceTargetVersion());
    executeParam.setInput(inputData);
    executeParam.setTargetType(target.getEvalTargetType());
    return executeParam;
  }

  private EvalTargetExecuteResult executeTargetWithErrorHandling(ISourceEvalTargetOperateService operator,
      Long spaceId, ExecuteEvalTargetParam executeParam) {
    try {
      return operator.execute(spaceId, executeParam);
    } catch (Exception e) {
      return createErrorResult(e);
    }
  }

  private EvalTargetExecuteResult createErrorResult(Exception e) {
    EvalTargetExecuteResult result = new EvalTargetExecuteResult();
    result.setStatus(EvalTargetRunStatus.FAIL);
    result.setOutputData(createErrorOutputData(e));
    return result;
  }

  private EvalTargetRecord createAndSaveRecord(Long spaceId, Long targetId, Long targetVersionId,
      ExecuteTargetCtx param, EvalTargetInputData inputData, EvalTargetExecuteResult result) {
    EvalTargetRecord record = createEvalTargetRecord(spaceId, targetId, targetVersionId,
      param, inputData, result.getOutputData(), result.getStatus());
    record.setId(idGenerator.genId());
    evalTargetRepo.createEvalTargetRecord(record);
    return record;
  }

  @Override
  public EvalTargetRecord getRecordById(Long spaceId, Long recordId) {
    return evalTargetRepo.getEvalTargetRecordByIdAndSpaceId(spaceId, recordId);
  }

  @Override
  public List<EvalTargetRecord> batchGetRecordByIds(Long spaceId, List<Long> recordIds) {
    if (spaceId == null) {
      throw new BssException("空间ID不能为空");
    }

    return evalTargetRepo.listEvalTargetRecordByIdsAndSpaceId(spaceId, recordIds);
  }

  /**
   * 创建错误输出数据
   */
  private EvalTargetOutputData createErrorOutputData(Exception error) {
    EvalTargetOutputData outputData = new EvalTargetOutputData();
    outputData.setOutputFields(Map.of());

    EvalTargetUsage usage = new EvalTargetUsage();
    usage.setInputTokens(0L);
    usage.setOutputTokens(0L);
    outputData.setEvalTargetUsage(usage);

    EvalTargetRunError runError = new EvalTargetRunError();
    runError.setCode(500);
    runError.setMessage(error.getMessage());
    outputData.setEvalTargetRunError(runError);

    outputData.setTimeConsumingMs(0L);
    return outputData;
  }

  /**
   * 创建评测记录
   */
  private EvalTargetRecord createEvalTargetRecord(Long spaceId, Long targetId, Long targetVersionId,
                                                  ExecuteTargetCtx param, EvalTargetInputData inputData,
                                                  EvalTargetOutputData outputData,
                                                  EvalTargetRunStatus runStatus) {
    EvalTargetRecord record = new EvalTargetRecord();
    record.setSpaceId(spaceId);
    record.setTargetId(targetId);
    record.setTargetVersionId(targetVersionId);
    record.setExperimentRunId(param.getExperimentRunId());
    record.setItemId(param.getItemId());
    record.setTurnId(param.getTurnId());
    record.setTraceId(generateTraceId());
    record.setLogId(generateLogId());
    record.setEvalTargetInputData(inputData);
    record.setEvalTargetOutputData(outputData);
    record.setStatus(runStatus);

    // 注入用户信息
    String userIDInContext = SessionContext.getCurrentUserId();
    BaseInfo baseInfo = new BaseInfo();
    UserInfo createdBy = new UserInfo();
    UserInfo updatedBy = new UserInfo();
    createdBy.setUserId(userIDInContext);
    updatedBy.setUserId(userIDInContext);
    baseInfo.setCreatedAt(System.currentTimeMillis());
    baseInfo.setUpdatedAt(System.currentTimeMillis());
    baseInfo.setCreatedBy(createdBy);
    baseInfo.setUpdatedBy(updatedBy);

    record.setBaseInfo(baseInfo);

    return record;
  }

  /**
   * 生成链路ID
   */
  private String generateTraceId() {
    return String.valueOf(idGenerator.genId());
  }

  /**
   * 生成日志ID
   */
  private String generateLogId() {
    return String.valueOf(idGenerator.genId());
  }
}
