package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.impl;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorVersionEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorVersionParam;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.EvaluatorVersionDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.dto.ListEvaluatorVersionResponse;
import com.iwhalecloud.bote.mapper.loop.evaluation.EvaluatorVersionMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

/**
 * 评估器版本数据访问对象实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/evaluator_version.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class EvaluatorVersionDAOImpl implements EvaluatorVersionDAO {
  private final EvaluatorVersionMapper evaluatorVersionMapper;

  @Override
  public int createEvaluatorVersion(EvaluatorVersionEntity version) {
    try {
      return evaluatorVersionMapper.createEvaluatorVersion(version);
    }
    catch (Exception e) {
      throw new BssException("创建评估器版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public int updateEvaluatorDraft(EvaluatorVersionEntity version) {
    try {
      return evaluatorVersionMapper.updateEvaluatorDraft(version);
    }
    catch (Exception e) {
      throw new BssException("更新评估器草稿失败: " + e.getMessage(), e);
    }
  }

  @Override
  public int deleteEvaluatorVersion(Long id, String userId) {
    try {
      return evaluatorVersionMapper.deleteEvaluatorVersion(id, userId);
    }
    catch (Exception e) {
      throw new BssException("删除评估器版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public int batchDeleteEvaluatorVersionByEvaluatorIds(List<Long> evaluatorIds, String userId) {
    try {
      return evaluatorVersionMapper.batchDeleteEvaluatorVersionByEvaluatorIds(evaluatorIds, userId);
    }
    catch (Exception e) {
      throw new BssException("根据评估器ID批量删除版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public ListEvaluatorVersionResponse listEvaluatorVersion(ListEvaluatorVersionParam request) {
    try {
      RowBounds rowBounds = RowBounds.DEFAULT;
      if (request.getPageSize() != null && request.getPageSize() > 0 && request.getPageNum() != null && request.getPageNum() > 0) {
        int offset = (request.getPageNum() - 1) * request.getPageSize();
        rowBounds = new RowBounds(offset, request.getPageSize());
      }
      //noinspection resource
      Page<EvaluatorVersionEntity> page = evaluatorVersionMapper.listEvaluatorVersion(request, rowBounds); //NOPMD - suppressed CloseResource - 不需要关闭
      return ListEvaluatorVersionResponse.builder()
        .totalCount(page.getTotal())
        .versions(page.getResult())
        .build();
    }
    catch (Exception e) {
      throw new BssException("分页查询评估器版本列表失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<EvaluatorVersionEntity> batchGetEvaluatorVersionById(Long spaceId, List<Long> ids, boolean includeDeleted) {
    try {
      if (ids == null || ids.isEmpty()) {
        return null;
      }
      return evaluatorVersionMapper.batchGetEvaluatorVersionById(spaceId, ids, includeDeleted);
    }
    catch (Exception e) {
      throw new BssException("批量根据ID获取评估器版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<EvaluatorVersionEntity> batchGetEvaluatorDraftByEvaluatorId(List<Long> evaluatorIds, boolean includeDeleted) {
    try {
      if (evaluatorIds == null || evaluatorIds.isEmpty()) {
        return null;
      }
      return evaluatorVersionMapper.batchGetEvaluatorDraftByEvaluatorId(evaluatorIds, includeDeleted);
    }
    catch (Exception e) {
      throw new BssException("根据评估器ID批量获取草稿版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<EvaluatorVersionEntity> batchGetEvaluatorVersionsByEvaluatorIds(List<Long> evaluatorIds, boolean includeDeleted) {
    try {
      if (evaluatorIds == null || evaluatorIds.isEmpty()) {
        return null;
      }
      return evaluatorVersionMapper.batchGetEvaluatorVersionsByEvaluatorIds(evaluatorIds, includeDeleted);
    }
    catch (Exception e) {
      throw new BssException("根据评估器ID批量获取版本失败: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean checkVersionExist(Long evaluatorId, String version) {
    try {
      return evaluatorVersionMapper.checkVersionExist(evaluatorId, version);
    }
    catch (Exception e) {
      throw new BssException("检查版本是否存在失败: " + e.getMessage(), e);
    }
  }
}
