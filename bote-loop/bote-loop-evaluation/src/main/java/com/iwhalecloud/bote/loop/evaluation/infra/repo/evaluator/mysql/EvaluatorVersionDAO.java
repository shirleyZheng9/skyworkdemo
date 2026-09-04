package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorVersionEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorVersionParam;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.dto.ListEvaluatorVersionResponse;
import java.util.List;

/**
 * 评估器版本数据访问对象接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/evaluator_version.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface EvaluatorVersionDAO {

  /**
   * 创建评估器版本
   * 迁移对应关系: Go语言EvaluatorVersionDAO.CreateEvaluatorVersion
   *
   * @param version 评估器版本实体
   * @return 影响行数
   */
  int createEvaluatorVersion(EvaluatorVersionEntity version);

  /**
   * 更新评估器草稿
   * 迁移对应关系: Go语言EvaluatorVersionDAO.UpdateEvaluatorDraft
   *
   * @param version 评估器版本实体
   * @return 影响行数
   */
  int updateEvaluatorDraft(EvaluatorVersionEntity version);

  /**
   * 删除评估器版本
   * 迁移对应关系: Go语言EvaluatorVersionDAO.DeleteEvaluatorVersion
   *
   * @param id 版本ID
   * @param userId 用户ID
   * @return 影响行数
   */
  int deleteEvaluatorVersion(Long id, String userId);

  /**
   * 根据评估器ID批量删除版本
   * 迁移对应关系: Go语言EvaluatorVersionDAO.BatchDeleteEvaluatorVersionByEvaluatorIDs
   *
   * @param evaluatorIds 评估器ID列表
   * @param userId 用户ID
   * @return 影响行数
   */
  int batchDeleteEvaluatorVersionByEvaluatorIds(List<Long> evaluatorIds, String userId);

  /**
   * 分页查询评估器版本列表
   * 迁移对应关系: Go语言EvaluatorVersionDAO.ListEvaluatorVersion
   *
   * @param request 查询请求
   * @return 查询响应
   */
  ListEvaluatorVersionResponse listEvaluatorVersion(ListEvaluatorVersionParam request);

  /**
   * 批量根据ID获取评估器版本
   * 迁移对应关系: Go语言EvaluatorVersionDAO.BatchGetEvaluatorVersionByID
   *
   * @param spaceId 空间ID
   * @param ids 版本ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器版本实体列表
   */
  List<EvaluatorVersionEntity> batchGetEvaluatorVersionById(Long spaceId, List<Long> ids, boolean includeDeleted);

  /**
   * 根据评估器ID批量获取草稿版本
   * 迁移对应关系: Go语言EvaluatorVersionDAO.BatchGetEvaluatorDraftByEvaluatorID
   *
   * @param evaluatorIds 评估器ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器版本实体列表
   */
  List<EvaluatorVersionEntity> batchGetEvaluatorDraftByEvaluatorId(List<Long> evaluatorIds, boolean includeDeleted);

  /**
   * 根据评估器ID批量获取版本
   * 迁移对应关系: Go语言EvaluatorVersionDAO.BatchGetEvaluatorVersionsByEvaluatorIDs
   *
   * @param evaluatorIds 评估器ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器版本实体列表
   */
  List<EvaluatorVersionEntity> batchGetEvaluatorVersionsByEvaluatorIds(List<Long> evaluatorIds, boolean includeDeleted);

  /**
   * 检查版本是否存在
   * 迁移对应关系: Go语言EvaluatorVersionDAO.CheckVersionExist
   *
   * @param evaluatorId 评估器ID
   * @param version 版本号
   * @return 是否存在
   */
  boolean checkVersionExist(Long evaluatorId, String version);
}
