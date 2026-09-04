package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Correction;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.AggrCalculateEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.CalculateMode;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.SpecificFieldInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.event.ExptEventPublisher;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IEvaluatorRecordRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorRecordService;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 评估器记录服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/evaluator_record_impl.go
 * - 功能: 评估器记录业务逻辑实现
 * - 主要方法:
 * * correctEvaluatorRecord - 修正评估器记录
 * * getEvaluatorRecord - 获取评估器记录
 * * batchGetEvaluatorRecord - 批量获取评估器记录
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluatorRecordServiceImpl结构体
 * - 使用Spring Service注解
 * - 依赖多个REPO和事件发布器
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
public class EvaluatorRecordServiceImpl implements EvaluatorRecordService {
  private final IEvaluatorRecordRepo evaluatorRecordRepo;
  private final ExptEventPublisher exptPublisher;

  @Override
  public void correctEvaluatorRecord(EvaluatorRecord evaluatorRecordDO, Correction correctionDO) {
    String userIDInContext = SessionContext.getCurrentUserId();
    correctionDO.setUpdatedBy(userIDInContext);

    // 确保EvaluatorOutputData不为空
    if (evaluatorRecordDO.getEvaluatorOutputData() == null) {
      evaluatorRecordDO.setEvaluatorOutputData(new EvaluatorOutputData());
    }

    // 确保EvaluatorResult不为空
    if (evaluatorRecordDO.getEvaluatorOutputData().getEvaluatorResult() == null) {
      evaluatorRecordDO.getEvaluatorOutputData().setEvaluatorResult(new EvaluatorResult());
    }

    // 设置修正信息
    evaluatorRecordDO.getEvaluatorOutputData().getEvaluatorResult().setCorrection(correctionDO);

    // 确保BaseInfo不为空
    if (evaluatorRecordDO.getBaseInfo() == null) {
      evaluatorRecordDO.setBaseInfo(new BaseInfo());
    }

    // 更新BaseInfo
    evaluatorRecordDO.setBaseInfo(BaseInfo.builder()
      .updatedBy(UserInfo.builder()
        .userId(userIDInContext)
        .build())
      .updatedAt(System.currentTimeMillis())
      .build());
    // 修正评估器记录
    evaluatorRecordRepo.correctEvaluatorRecord(evaluatorRecordDO);
    // 发送聚合报告计算消息
    String evaluatorVersionIDStr = String.valueOf(evaluatorRecordDO.getEvaluatorVersionId());
    exptPublisher.publishExptAggrCalculateEvent(List.of(AggrCalculateEvent.builder()
      .experimentId(evaluatorRecordDO.getExperimentId())
      .spaceId(evaluatorRecordDO.getSpaceId())
      .calculateMode(CalculateMode.CREATE_ALL_FIELDS)
      .specificFieldInfo(SpecificFieldInfo.builder()
        .fieldKey(evaluatorVersionIDStr)
        .fieldType(FieldType.EVALUATOR_SCORE)
        .build())
      .build()), Duration.ofSeconds(3L));
  }

  private static final Logger logger = LoggerFactory.getLogger(EvaluatorRecordServiceImpl.class);

  @Override
  public EvaluatorRecord getEvaluatorRecord(Long evaluatorRecordId, Boolean includeDeleted) {
    try {
      return evaluatorRecordRepo.getEvaluatorRecord(evaluatorRecordId, includeDeleted);
    }
    catch (Exception e) {
      throw new BssException("获取评估器记录失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<EvaluatorRecord> batchGetEvaluatorRecord(List<Long> evaluatorRecordIds, Boolean includeDeleted) {
    return evaluatorRecordRepo.batchGetEvaluatorRecord(evaluatorRecordIds, includeDeleted);
  }
}
