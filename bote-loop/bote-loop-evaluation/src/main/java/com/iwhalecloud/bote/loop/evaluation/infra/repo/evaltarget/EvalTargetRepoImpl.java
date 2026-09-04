package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.TargetRecordEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.TargetVersionEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IEvalTargetRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.BatchGetEvalTargetBySourceParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.CreateEvalTargetResult;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.EvalTargetDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.EvalTargetRecordDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.EvalTargetVersionDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.convertor.EvalTargetConvertor;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaltarget.mysql.convertor.EvalTargetRecordConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评估目标仓库实现类
 * 对应Go: EvalTargetRepoImpl
 */
@Repository
@RequiredArgsConstructor
public class EvalTargetRepoImpl implements IEvalTargetRepo {
  private final EvalTargetDAO evalTargetDao;
  private final EvalTargetVersionDAO evalTargetVersionDao;
  private final EvalTargetRecordDAO evalTargetRecordDao;
  private final IIDGenerator idGenerator;

  /**
   * 创建评估目标
   * 对应Go: CreateEvalTarget(ctx context.Context, do *entity.EvalTarget) (id, versionID int64, err error)
   */
  @Override
  @Transactional
  public CreateEvalTargetResult createEvalTarget(EvalTarget evalTarget) {
    if (evalTarget == null) {
      throw new BssException("evalTarget is null");
    }
    if (evalTarget.getEvalTargetVersion() == null) {
      throw new BssException("evalTargetVersion is null");
    }

    // 生成主键ID
    List<Long> genIds = idGenerator.genMultiIds(2);
    Long id = genIds.get(0);
    Long versionId = genIds.get(1);

    try {
      // 检查是否创建过这个对象
      TargetEntity target = evalTargetDao.getEvalTargetBySourceId(
        evalTarget.getSpaceId(),
        evalTarget.getSourceTargetId(),
        evalTarget.getEvalTargetType().getValue());

      // 如果没有创建过，则创建
      if (target == null) {
        evalTarget.setId(id);
        TargetEntity targetEntity = EvalTargetConvertor.convertDO2PO(evalTarget);
        evalTargetDao.createEvalTarget(targetEntity);
      }
      else {
        id = target.getId();
      }

      // 检查这个对象的版本是否创建过
      TargetVersionEntity version = evalTargetVersionDao.getEvalTargetVersionByTarget(
        evalTarget.getSpaceId(),
        id,
        evalTarget.getEvalTargetVersion().getSourceTargetVersion());

      // 如果版本没有创建过，则创建
      if (version == null) {
        evalTarget.getEvalTargetVersion().setId(versionId);
        evalTarget.getEvalTargetVersion().setTargetId(id);
        TargetVersionEntity versionEntity = EvalTargetConvertor.convertVersionDO2PO(evalTarget.getEvalTargetVersion());
        evalTargetVersionDao.createEvalTargetVersion(versionEntity);
      }
      else {
        versionId = version.getId();
      }

      return CreateEvalTargetResult.builder().id(id).versionId(versionId).build();
    }
    catch (Exception e) {
      throw new BssException("Failed to create eval target: " + e.getMessage(), e);
    }
  }

  /**
   * 获取评估目标
   * 对应Go: GetEvalTarget(ctx context.Context, targetID int64) (do *entity.EvalTarget, err error)
   */
  @Override
  public EvalTarget getEvalTarget(Long targetId) {
    try {
      TargetEntity target = evalTargetDao.getEvalTarget(targetId);
      if (target == null) {
        return null;
      }
      return EvalTargetConvertor.convertPO2DO(target);
    }
    catch (Exception e) {
      throw new BssException("Failed to get eval target: " + e.getMessage(), e);
    }
  }

  /**
   * 获取评估目标版本
   * 对应Go: GetEvalTargetVersion(ctx context.Context, spaceID, versionID int64) (do *entity.EvalTarget, err error)
   */
  @Override
  public EvalTarget getEvalTargetVersion(Long spaceId, Long versionId) {
    try {
      TargetVersionEntity versionPO = evalTargetVersionDao.getEvalTargetVersion(spaceId, versionId);
      if (versionPO == null) {
        throw new BssException("Resource not found");
      }

      TargetEntity targetPO = evalTargetDao.getEvalTarget(versionPO.getTargetId());
      if (targetPO == null) {
        throw new BssException("Resource not found");
      }

      EvalTarget targetDO = EvalTargetConvertor.convertPO2DO(targetPO);
      EvalTargetVersion versionDO = EvalTargetConvertor.convertVersionPO2DO(versionPO, targetDO.getEvalTargetType());
      targetDO.setEvalTargetVersion(versionDO);

      return targetDO;
    }
    catch (Exception e) {
      throw new BssException("Failed to get eval target version: " + e.getMessage(), e);
    }
  }

  /**
   * 根据来源批量获取评估目标
   * 对应Go: BatchGetEvalTargetBySource(ctx context.Context, param *repo.BatchGetEvalTargetBySourceParam) (dos []*entity.EvalTarget, err error)
   */
  @Override
  public List<EvalTarget> batchGetEvalTargetBySource(BatchGetEvalTargetBySourceParam param) {
    try {
      List<TargetEntity> targets = evalTargetDao.batchGetEvalTargetBySource(
        param.getSpaceId(),
        param.getSourceTargetId(),
        param.getTargetType().getValue());

      if (targets == null || targets.isEmpty()) {
        return Collections.emptyList();
      }

      return EvalTargetConvertor.convertPOs2DOs(targets);
    }
    catch (Exception e) {
      throw new BssException("Failed to batch get eval target by source: " + e.getMessage(), e);
    }
  }

  /**
   * 批量获取评估目标版本
   * 对应Go: BatchGetEvalTargetVersion(ctx context.Context, spaceID int64, versionIDs []int64) (dos []*entity.EvalTarget, err error)
   */
  @Override
  public List<EvalTarget> batchGetEvalTargetVersion(Long spaceId, List<Long> versionIds) {
    try {
      List<TargetVersionEntity> versions = evalTargetVersionDao.batchGetEvalTargetVersion(spaceId, versionIds);
      if (versions == null || versions.isEmpty()) {
        return null;
      }

      List<Long> targetIds = new ArrayList<>();
      for (TargetVersionEntity version : versions) {
        targetIds.add(version.getTargetId());
      }

      List<TargetEntity> targets = evalTargetDao.batchGetEvalTarget(spaceId, targetIds);
      if (targets == null || targets.isEmpty()) {
        return null;
      }

      Map<Long, TargetEntity> targetMap = new HashMap<>();
      for (TargetEntity target : targets) {
        targetMap.put(target.getId(), target);
      }

      List<EvalTarget> evalTargets = new ArrayList<>();
      for (TargetVersionEntity version : versions) {
        TargetEntity target = targetMap.get(version.getTargetId());
        if (target == null) {
          continue;
        }

        EvalTarget targetDO = EvalTargetConvertor.convertPO2DO(target);
        EvalTargetVersion versionDO = EvalTargetConvertor.convertVersionPO2DO(version, targetDO.getEvalTargetType());
        targetDO.setEvalTargetVersion(versionDO);
        evalTargets.add(targetDO);
      }

      return evalTargets;
    }
    catch (Exception e) {
      throw new BssException("Failed to batch get eval target version: " + e.getMessage(), e);
    }
  }

  /**
   * 创建评估目标记录
   * 对应Go: CreateEvalTargetRecord(ctx context.Context, record *entity.EvalTargetRecord) (int64, error)
   */
  @Override
  public Long createEvalTargetRecord(EvalTargetRecord record) {
    try {
      TargetRecordEntity po = EvalTargetRecordConvertor.convertDO2PO(record);
      po.setDeletedAt(0L);
      return evalTargetRecordDao.create(po);
    }
    catch (Exception e) {
      throw new BssException("Failed to create eval target record: " + e.getMessage(), e);
    }
  }

  /**
   * 根据ID和空间ID获取评估目标记录
   * 对应Go: GetEvalTargetRecordByIDAndSpaceID(ctx context.Context, spaceID int64, recordID int64) (*entity.EvalTargetRecord, error)
   */
  @Override
  public EvalTargetRecord getEvalTargetRecordByIdAndSpaceId(Long spaceId, Long recordId) {
    try {
      TargetRecordEntity recordPO = evalTargetRecordDao.getByIdAndSpaceId(recordId, spaceId);
      if (recordPO == null) {
        return null;
      }
      return EvalTargetRecordConvertor.convertPO2DO(recordPO);
    }
    catch (Exception e) {
      throw new BssException("Failed to get eval target record by id and space id: " + e.getMessage(), e);
    }
  }

  /**
   * 根据ID列表和空间ID列出评估目标记录
   * 对应Go: ListEvalTargetRecordByIDsAndSpaceID(ctx context.Context, spaceID int64, recordIDs []int64) ([]*entity.EvalTargetRecord, error)
   */
  @Override
  public List<EvalTargetRecord> listEvalTargetRecordByIdsAndSpaceId(Long spaceId, List<Long> recordIds) {
    try {
      List<TargetRecordEntity> recordPOList = evalTargetRecordDao.listByIdsAndSpaceId(recordIds, spaceId);
      if (recordPOList == null || recordPOList.isEmpty()) {
        return new ArrayList<>();
      }

      List<EvalTargetRecord> result = new ArrayList<>();
      for (TargetRecordEntity recordPO : recordPOList) {
        EvalTargetRecord recordDO = EvalTargetRecordConvertor.convertPO2DO(recordPO);
        result.add(recordDO);
      }

      return result;
    }
    catch (Exception e) {
      throw new BssException("Failed to list eval target record by ids and space id: " + e.getMessage(), e);
    }
  }
}
