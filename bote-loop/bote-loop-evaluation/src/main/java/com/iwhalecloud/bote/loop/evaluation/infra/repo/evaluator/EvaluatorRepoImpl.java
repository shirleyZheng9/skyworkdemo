package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorVersionEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorType;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IEvaluatorRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorResponse;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorVersionResponse;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.EvaluatorDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.EvaluatorVersionDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.convertor.EvaluatorConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评估器仓储实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/evaluator_impl.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class EvaluatorRepoImpl implements IEvaluatorRepo {
  private final EvaluatorDAO evaluatorDAO;
  private final EvaluatorVersionDAO evaluatorVersionDAO;
  private final IIDGenerator iidGenerator;
  private final TenantSettingInfoCache tenantSettingInfoCache;

  @Override
  @Transactional
  public Long createEvaluator(Evaluator evaluator) {
    try {
      // 生成主键ID
      Long evaluatorId = iidGenerator.genId();
      Long versionId = iidGenerator.genId();
      Long draftVersionId = iidGenerator.genId();

      EvaluatorEntity evaluatorPO = EvaluatorConvertor.convertToPO(evaluator);
      evaluatorPO.setId(evaluatorId);
      evaluatorPO.setDraftSubmitted(true); // 初始化创建时草稿统一已提交
      evaluatorPO.setLatestVersion(evaluator.getEvaluatorVersion().getVersion());

      EvaluatorVersionEntity evaluatorVersionPO = EvaluatorConvertor.convertVersionToPO(evaluator);
      evaluatorVersionPO.setId(versionId);
      evaluatorVersionPO.setEvaluatorId(evaluatorId);

      // 创建评估器
      evaluatorDAO.createEvaluator(evaluatorPO);

      // 创建版本
      evaluatorVersionDAO.createEvaluatorVersion(evaluatorVersionPO);

      // 创建草稿版本
      evaluatorVersionPO.setId(draftVersionId);
      evaluatorVersionPO.setVersion("draft");
      evaluatorVersionPO.setDescription("");
      evaluatorVersionDAO.createEvaluatorVersion(evaluatorVersionPO);

      return evaluatorId;
    }
    catch (Exception e) {
      throw new BssException("创建评估器失败: " + e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void submitEvaluatorVersion(Evaluator evaluator) {
    try {
      // 更新评估器最新版本
      evaluatorDAO.updateEvaluatorLatestVersion(
        evaluator.getId(),
        evaluator.getEvaluatorVersion().getVersion(),
        evaluator.getBaseInfo().getUpdatedBy().getUserId()
      );

      EvaluatorVersionEntity evaluatorVersionPO = EvaluatorConvertor.convertVersionToPO(evaluator);
      evaluatorVersionDAO.createEvaluatorVersion(evaluatorVersionPO);
    }
    catch (Exception e) {
      throw new BssException("提交评估器版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void updateEvaluatorDraft(Evaluator evaluator) {
    try {
      EvaluatorVersionEntity evaluatorVersionPO = EvaluatorConvertor.convertVersionToPO(evaluator);

      // 更新评估器草稿提交状态
      evaluatorDAO.updateEvaluatorDraftSubmitted(
        evaluatorVersionPO.getEvaluatorId(),
        false,
        evaluator.getBaseInfo().getUpdatedBy().getUserId()
      );

      evaluatorVersionDAO.updateEvaluatorDraft(evaluatorVersionPO);
    }
    catch (Exception e) {
      throw new BssException("更新评估器草稿失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Evaluator> batchGetEvaluatorMetaById(List<Long> ids, Boolean includeDeleted) {
    try {
      boolean includeDeletedValue = includeDeleted != null && includeDeleted;
      List<EvaluatorEntity> evaluatorPOS = evaluatorDAO.batchGetEvaluatorById(ids, includeDeletedValue);
      return evaluatorPOS.stream()
        .map(EvaluatorConvertor::convertToDO)
        .collect(Collectors.toList());
    }
    catch (Exception e) {
      throw new BssException("批量根据ID获取评估器元信息失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Evaluator> batchGetEvaluatorByVersionId(Long spaceId, List<Long> ids, Boolean includeDeleted) {
    try {
      boolean includeDeletedValue = includeDeleted != null && includeDeleted;
      List<EvaluatorVersionEntity> evaluatorVersionPOS = evaluatorVersionDAO.batchGetEvaluatorVersionById(spaceId, ids, includeDeletedValue);
      if (evaluatorVersionPOS == null || evaluatorVersionPOS.isEmpty()) {
        return new ArrayList<>();
      }
      List<Long> evaluatorIds = evaluatorVersionPOS.stream()
        .map(EvaluatorVersionEntity::getEvaluatorId)
        .collect(Collectors.toList());

      List<EvaluatorEntity> evaluatorPOS = evaluatorDAO.batchGetEvaluatorById(evaluatorIds, includeDeletedValue);
      Map<Long, EvaluatorEntity> evaluatorMap = evaluatorPOS.stream()
        .collect(Collectors.toMap(EvaluatorEntity::getId, po -> po));

      return evaluatorVersionPOS.stream()
        .filter(versionPO -> versionPO.getEvaluatorType() != null)
        .filter(versionPO -> versionPO.getEvaluatorType().equals(EvaluatorType.PROMPT.getValue()))
        .map(versionPO -> {
          Evaluator evaluatorDO = EvaluatorConvertor.convertToDO(evaluatorMap.get(versionPO.getEvaluatorId()));
          Evaluator evaluatorVersion = EvaluatorConvertor.convertVersionToDO(versionPO);
          if (evaluatorVersion.getPromptEvaluatorVersion().getModelConfig().getModelId() == -1L) {
            evaluatorVersion.getPromptEvaluatorVersion().getModelConfig().setModelId(tenantSettingInfoCache.getModelId(spaceId));
          }
          evaluatorDO.setEvaluatorVersion(evaluatorVersion);
          evaluatorDO.setEvaluatorType(EvaluatorType.PROMPT);
          return evaluatorDO;
        })
        .collect(Collectors.toList());
    }
    catch (Exception e) {
      throw new BssException("批量根据版本ID获取评估器失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Evaluator> batchGetEvaluatorDraftByEvaluatorId(Long spaceId, List<Long> ids, Boolean includeDeleted) {
    try {
      boolean includeDeletedValue = includeDeleted != null && includeDeleted;
      List<EvaluatorVersionEntity> evaluatorVersionPOS = evaluatorVersionDAO.batchGetEvaluatorDraftByEvaluatorId(ids, includeDeletedValue);
      Map<Long, EvaluatorVersionEntity> evaluatorId2VersionPO = evaluatorVersionPOS.stream()
        .collect(Collectors.toMap(EvaluatorVersionEntity::getEvaluatorId, po -> po));

      List<EvaluatorEntity> evaluatorPOS = evaluatorDAO.batchGetEvaluatorById(ids, includeDeletedValue);

      return evaluatorPOS.stream()
        .map(evaluatorPO -> {
          Evaluator evaluatorDO = EvaluatorConvertor.convertToDO(evaluatorPO);
          EvaluatorVersionEntity evaluatorVersionPO = evaluatorId2VersionPO.get(evaluatorPO.getId());
          if (evaluatorVersionPO != null) {
            Evaluator evaluatorVersion = EvaluatorConvertor.convertVersionToDO(evaluatorVersionPO);
            if (evaluatorVersion.getPromptEvaluatorVersion().getModelConfig().getModelId() == -1L) {
              evaluatorVersion.getPromptEvaluatorVersion().getModelConfig().setModelId(tenantSettingInfoCache.getModelId(spaceId));
            }
            evaluatorDO.setEvaluatorVersion(evaluatorVersion);
          }
          return evaluatorDO;
        })
        .collect(Collectors.toList());
    }
    catch (Exception e) {
      throw new BssException("批量根据评估器ID获取草稿失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Evaluator> batchGetEvaluatorVersionsByEvaluatorIds(List<Long> evaluatorIds, Boolean includeDeleted) {
    try {
      boolean includeDeletedValue = includeDeleted != null && includeDeleted;
      List<EvaluatorVersionEntity> evaluatorVersionPOS = evaluatorVersionDAO.batchGetEvaluatorVersionsByEvaluatorIds(evaluatorIds, includeDeletedValue);
      return evaluatorVersionPOS.stream()
        .map(EvaluatorConvertor::convertVersionToDO)
        .collect(Collectors.toList());
    }
    catch (Exception e) {
      throw new BssException("批量根据评估器ID获取版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public ListEvaluatorVersionResponse listEvaluatorVersion(ListEvaluatorVersionParam request) {
    try {
      com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.dto.ListEvaluatorVersionResponse daoResp = evaluatorVersionDAO.listEvaluatorVersion(request);

      List<Evaluator> evaluatorVersionDOList = daoResp.getVersions().stream()
        .map(EvaluatorConvertor::convertVersionToDO)
        .collect(Collectors.toList());

      return ListEvaluatorVersionResponse.builder()
        .totalCount(daoResp.getTotalCount())
        .versions(evaluatorVersionDOList)
        .build();
    }
    catch (Exception e) {
      throw new BssException("分页查询评估器版本列表失败: " + e.getMessage(), e);
    }
  }

  @Override
  public Boolean checkVersionExist(Long evaluatorId, String version) {
    try {
      return evaluatorVersionDAO.checkVersionExist(evaluatorId, version);
    }
    catch (Exception e) {
      throw new BssException("检查版本是否存在失败: " + e.getMessage(), e);
    }
  }

  public List<Evaluator> batchGetEvaluatorDraft(List<Long> ids, boolean includeDeleted) {
    try {
      if (ids == null || ids.isEmpty()) {
        return null;
      }

      List<EvaluatorEntity> evaluatorPOList = evaluatorDAO.batchGetEvaluatorById(ids, includeDeleted);
      List<EvaluatorVersionEntity> evaluatorVersionPOList = evaluatorVersionDAO.batchGetEvaluatorVersionById(null, ids, includeDeleted);

      Map<Long, Evaluator> evaluatorVersionDOMap = evaluatorVersionPOList.stream()
        .collect(Collectors.toMap(
          EvaluatorVersionEntity::getEvaluatorId,
          EvaluatorConvertor::convertVersionToDO
        ));

      return evaluatorPOList.stream()
        .map(evaluatorPO -> {
          Evaluator evaluatorDO = EvaluatorConvertor.convertToDO(evaluatorPO);
          Evaluator evaluatorVersionDO = evaluatorVersionDOMap.get(evaluatorPO.getId());
          if (evaluatorVersionDO != null) {
            evaluatorDO.setEvaluatorVersion(evaluatorDO);
          }
          return evaluatorDO;
        })
        .collect(Collectors.toList());
    }
    catch (Exception e) {
      throw new BssException("批量根据ID获取评估器草稿失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void updateEvaluatorMeta(Long id, String name, String description, String userId, Long catalogItemId) {
    try {
      EvaluatorEntity po = EvaluatorEntity.builder()
        .id(id)
        .name(name)
        .description(description)
        .updatedBy(userId)
        .catalogItemId(catalogItemId)
        .build();
      evaluatorDAO.updateEvaluatorMeta(po);
    }
    catch (Exception e) {
      throw new BssException("更新评估器元信息失败: " + e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void batchDeleteEvaluator(List<Long> ids, String userId) {
    try {
      evaluatorDAO.batchDeleteEvaluator(ids, userId);
      evaluatorVersionDAO.batchDeleteEvaluatorVersionByEvaluatorIds(ids, userId);
    }
    catch (Exception e) {
      throw new BssException("批量删除评估器失败: " + e.getMessage(), e);
    }
  }

  @Override
  public Boolean checkNameExist(Long spaceId, Long evaluatorId, String name) {
    try {
      return evaluatorDAO.checkNameExist(spaceId, evaluatorId, name);
    }
    catch (Exception e) {
      throw new BssException("检查名称是否存在失败: " + e.getMessage(), e);
    }
  }

  @Override
  public ListEvaluatorResponse listEvaluator(ListEvaluatorParam request) {
    final com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.dto.ListEvaluatorResponse evaluatorPOS = evaluatorDAO.listEvaluator(request);
    List<Evaluator> evaluatorDOList = evaluatorPOS.getEvaluators().stream()
      .map(EvaluatorConvertor::convertToDO)
      .collect(Collectors.toList());
    return ListEvaluatorResponse.builder()
      .totalCount(evaluatorPOS.getTotalCount())
      .evaluators(evaluatorDOList)
      .build();
  }
}
