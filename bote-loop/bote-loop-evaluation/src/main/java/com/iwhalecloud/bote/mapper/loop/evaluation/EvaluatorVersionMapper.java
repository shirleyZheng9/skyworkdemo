package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorVersionEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorVersionParam;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 评估器版本Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/evaluator_version.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface EvaluatorVersionMapper {

  /**
   * 创建评估器版本
   * 迁移对应关系: Go语言EvaluatorVersionDAOImpl.CreateEvaluatorVersion
   *
   * @param version 评估器版本实体
   * @return 影响行数
   */
  int createEvaluatorVersion(@Param("version") EvaluatorVersionEntity version);

  /**
   * 更新评估器草稿
   * 迁移对应关系: Go语言EvaluatorVersionDAOImpl.UpdateEvaluatorDraft
   *
   * @param version 评估器版本实体
   * @return 影响行数
   */
  int updateEvaluatorDraft(@Param("version") EvaluatorVersionEntity version);

  /**
   * 删除评估器版本
   * 迁移对应关系: Go语言EvaluatorVersionDAOImpl.DeleteEvaluatorVersion
   *
   * @param id 版本ID
   * @param userId 用户ID
   * @return 影响行数
   */
  int deleteEvaluatorVersion(@Param("id") Long id, @Param("userId") String userId);

  /**
   * 根据评估器ID批量删除版本
   * 迁移对应关系: Go语言EvaluatorVersionDAOImpl.BatchDeleteEvaluatorVersionByEvaluatorIDs
   *
   * @param evaluatorIds 评估器ID列表
   * @param userId 用户ID
   * @return 影响行数
   */
  int batchDeleteEvaluatorVersionByEvaluatorIds(@Param("evaluatorIds") List<Long> evaluatorIds,
                                                @Param("userId") String userId);

  /**
   * 分页查询评估器版本列表
   * 迁移对应关系: Go语言EvaluatorVersionDAOImpl.ListEvaluatorVersion
   *
   * @param request 查询请求
   * @param rowBounds 分页参数
   * @return 评估器版本分页列表
   */
  Page<EvaluatorVersionEntity> listEvaluatorVersion(@Param("request") ListEvaluatorVersionParam request, RowBounds rowBounds);

  /**
   * 批量根据ID获取评估器版本
   * 迁移对应关系: Go语言EvaluatorVersionDAOImpl.BatchGetEvaluatorVersionByID
   *
   * @param spaceId 空间ID
   * @param ids 版本ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器版本实体列表
   */
  List<EvaluatorVersionEntity> batchGetEvaluatorVersionById(@Param("spaceId") Long spaceId,
                                                            @Param("ids") List<Long> ids,
                                                            @Param("includeDeleted") boolean includeDeleted);

  /**
   * 根据评估器ID批量获取草稿版本
   * 迁移对应关系: Go语言EvaluatorVersionDAOImpl.BatchGetEvaluatorDraftByEvaluatorID
   *
   * @param evaluatorIds 评估器ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器版本实体列表
   */
  List<EvaluatorVersionEntity> batchGetEvaluatorDraftByEvaluatorId(@Param("evaluatorIds") List<Long> evaluatorIds,
                                                                   @Param("includeDeleted") boolean includeDeleted);

  /**
   * 根据评估器ID批量获取版本
   * 迁移对应关系: Go语言EvaluatorVersionDAOImpl.BatchGetEvaluatorVersionsByEvaluatorIDs
   *
   * @param evaluatorIds 评估器ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器版本实体列表
   */
  List<EvaluatorVersionEntity> batchGetEvaluatorVersionsByEvaluatorIds(@Param("evaluatorIds") List<Long> evaluatorIds,
                                                                       @Param("includeDeleted") boolean includeDeleted);

  /**
   * 检查版本是否存在
   * 迁移对应关系: Go语言EvaluatorVersionDAOImpl.CheckVersionExist
   *
   * @param evaluatorId 评估器ID
   * @param version 版本号
   * @return 是否存在
   */
  boolean checkVersionExist(@Param("evaluatorId") Long evaluatorId, @Param("version") String version);
}
