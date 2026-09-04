package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.impl;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorParam;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.EvaluatorDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.dto.ListEvaluatorResponse;
import com.iwhalecloud.bote.mapper.loop.evaluation.EvaluatorMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

/**
 * 评估器数据访问对象实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/evaluator.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class EvaluatorDAOImpl implements EvaluatorDAO {
  private final EvaluatorMapper evaluatorMapper;

  @Override
  public int createEvaluator(EvaluatorEntity evaluator) {
    // 初始化创建时草稿统一已提交
    evaluator.setDraftSubmitted(true);
    return evaluatorMapper.createEvaluator(evaluator);
  }

  @Override
  public EvaluatorEntity getEvaluatorById(Long id, boolean includeDeleted) {
    return evaluatorMapper.getEvaluatorById(id, includeDeleted);
  }

  @Override
  public List<EvaluatorEntity> batchGetEvaluatorById(List<Long> ids, boolean includeDeleted) {
    return evaluatorMapper.batchGetEvaluatorById(ids, includeDeleted);
  }

  @Override
  public int updateEvaluatorMeta(EvaluatorEntity evaluator) {
    return evaluatorMapper.updateEvaluatorMeta(evaluator);

  }

  @Override
  public int updateEvaluatorDraftSubmitted(Long evaluatorId, Boolean draftSubmitted, String userId) {
    return evaluatorMapper.updateEvaluatorDraftSubmitted(evaluatorId, draftSubmitted, userId);
  }

  @Override
  public int batchDeleteEvaluator(List<Long> ids, String userId) {
    if (ids == null || ids.isEmpty()) {
      return 0;
    }
    return evaluatorMapper.batchDeleteEvaluator(ids, userId);
  }

  @Override
  public ListEvaluatorResponse listEvaluator(ListEvaluatorParam request) {
    RowBounds rowBounds = RowBounds.DEFAULT;
    if (request.getPageSize() != null && request.getPageSize() > 0 && request.getPageNum() != null && request.getPageNum() > 0) {
      int offset = (request.getPageNum() - 1) * request.getPageSize();
      rowBounds = new RowBounds(offset, request.getPageSize());
    }
    //noinspection resource
    Page<EvaluatorEntity> page = evaluatorMapper.listEvaluator(request, rowBounds); //NOPMD - suppressed CloseResource - 不需要关闭
    return ListEvaluatorResponse.builder()
      .totalCount(page.getTotal())
      .evaluators(page.getResult())
      .build();
  }

  @Override
  public boolean checkNameExist(Long spaceId, Long evaluatorId, String name) {
    return evaluatorMapper.checkNameExist(spaceId, evaluatorId, name);
  }

  @Override
  public int updateEvaluatorLatestVersion(Long evaluatorId, String version, String userId) {
    try {
      return evaluatorMapper.updateEvaluatorLatestVersion(evaluatorId, version, userId);
    }
    catch (Exception e) {
      throw new BssException("更新评估器最新版本失败: " + e.getMessage(), e);
    }
  }
}
