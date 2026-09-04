package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorParam;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.dto.ListEvaluatorResponse;
import java.util.List;

/**
 * 评估器数据访问对象接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/evaluator.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface EvaluatorDAO {

  /**
   * 创建评估器
   * 迁移对应关系: Go语言EvaluatorDAO.CreateEvaluator
   *
   * @param evaluator 评估器实体
   * @return 影响行数
   */
  int createEvaluator(EvaluatorEntity evaluator);

  /**
   * 根据ID获取评估器
   * 迁移对应关系: Go语言EvaluatorDAO.GetEvaluatorByID
   *
   * @param id 评估器ID
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器实体
   */
  EvaluatorEntity getEvaluatorById(Long id, boolean includeDeleted);

  /**
   * 批量根据ID获取评估器
   * 迁移对应关系: Go语言EvaluatorDAO.BatchGetEvaluatorByID
   *
   * @param ids 评估器ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器实体列表
   */
  List<EvaluatorEntity> batchGetEvaluatorById(List<Long> ids, boolean includeDeleted);

  /**
   * 更新评估器元信息
   * 迁移对应关系: Go语言EvaluatorDAO.UpdateEvaluatorMeta
   *
   * @param evaluator 评估器实体
   * @return 影响行数
   */
  int updateEvaluatorMeta(EvaluatorEntity evaluator);

  /**
   * 更新评估器草稿提交状态
   * 迁移对应关系: Go语言EvaluatorDAO.UpdateEvaluatorDraftSubmitted
   *
   * @param evaluatorId 评估器ID
   * @param draftSubmitted 草稿是否已提交
   * @param userId 用户ID
   * @return 影响行数
   */
  int updateEvaluatorDraftSubmitted(Long evaluatorId, Boolean draftSubmitted, String userId);

  /**
   * 批量删除评估器
   * 迁移对应关系: Go语言EvaluatorDAO.BatchDeleteEvaluator
   *
   * @param ids 评估器ID列表
   * @param userId 用户ID
   * @return 影响行数
   */
  int batchDeleteEvaluator(List<Long> ids, String userId);

  /**
   * 分页查询评估器列表
   * 迁移对应关系: Go语言EvaluatorDAO.ListEvaluator
   *
   * @param request 查询请求
   * @return 查询响应
   */
  ListEvaluatorResponse listEvaluator(ListEvaluatorParam request);

  /**
   * 检查名称是否存在
   * 迁移对应关系: Go语言EvaluatorDAO.CheckNameExist
   *
   * @param spaceId 空间ID
   * @param evaluatorId 评估器ID
   * @param name 名称
   * @return 是否存在
   */
  boolean checkNameExist(Long spaceId, Long evaluatorId, String name);

  /**
   * 更新评估器最新版本
   * 迁移对应关系: Go语言EvaluatorDAO.UpdateEvaluatorLatestVersion
   *
   * @param evaluatorId 评估器ID
   * @param version 版本号
   * @param userId 用户ID
   * @return 影响行数
   */
  int updateEvaluatorLatestVersion(Long evaluatorId, String version, String userId);
}
