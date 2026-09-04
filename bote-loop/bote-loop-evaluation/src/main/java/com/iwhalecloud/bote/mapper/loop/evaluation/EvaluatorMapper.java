package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorParam;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 评估器Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/evaluator/mysql/evaluator.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface EvaluatorMapper {

  /**
   * 创建评估器
   * 迁移对应关系: Go语言EvaluatorDAOImpl.CreateEvaluator
   *
   * @param evaluator 评估器实体
   * @return 影响行数
   */
  int createEvaluator(@Param("evaluator") EvaluatorEntity evaluator);

  /**
   * 根据ID获取评估器
   * 迁移对应关系: Go语言EvaluatorDAOImpl.GetEvaluatorByID
   *
   * @param id 评估器ID
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器实体
   */
  EvaluatorEntity getEvaluatorById(@Param("id") Long id, @Param("includeDeleted") boolean includeDeleted);

  /**
   * 批量根据ID获取评估器
   * 迁移对应关系: Go语言EvaluatorDAOImpl.BatchGetEvaluatorByID
   *
   * @param ids 评估器ID列表
   * @param includeDeleted 是否包含已删除记录
   * @return 评估器实体列表
   */
  List<EvaluatorEntity> batchGetEvaluatorById(@Param("ids") List<Long> ids, @Param("includeDeleted") boolean includeDeleted);

  /**
   * 更新评估器元信息
   * 迁移对应关系: Go语言EvaluatorDAOImpl.UpdateEvaluatorMeta
   *
   * @param evaluator 评估器实体
   * @return 影响行数
   */
  int updateEvaluatorMeta(@Param("evaluator") EvaluatorEntity evaluator);

  /**
   * 更新评估器草稿提交状态
   * 迁移对应关系: Go语言EvaluatorDAOImpl.UpdateEvaluatorDraftSubmitted
   *
   * @param evaluatorId 评估器ID
   * @param draftSubmitted 草稿是否已提交
   * @param userId 用户ID
   * @return 影响行数
   */
  int updateEvaluatorDraftSubmitted(@Param("evaluatorId") Long evaluatorId,
                                    @Param("draftSubmitted") Boolean draftSubmitted,
                                    @Param("userId") String userId);

  /**
   * 批量删除评估器
   * 迁移对应关系: Go语言EvaluatorDAOImpl.BatchDeleteEvaluator
   *
   * @param ids 评估器ID列表
   * @param userId 用户ID
   * @return 影响行数
   */
  int batchDeleteEvaluator(@Param("ids") List<Long> ids, @Param("userId") String userId);

  /**
   * 分页查询评估器列表
   * 迁移对应关系: Go语言EvaluatorDAOImpl.ListEvaluator
   *
   * @param request 查询请求
   * @param rowBounds 分页参数
   * @return 评估器分页列表
   */
  Page<EvaluatorEntity> listEvaluator(@Param("request") ListEvaluatorParam request, RowBounds rowBounds);

  /**
   * 检查名称是否存在
   * 迁移对应关系: Go语言EvaluatorDAOImpl.CheckNameExist
   *
   * @param spaceId 空间ID
   * @param evaluatorId 评估器ID
   * @param name 名称
   * @return 是否存在
   */
  boolean checkNameExist(@Param("spaceId") Long spaceId,
                         @Param("evaluatorId") Long evaluatorId,
                         @Param("name") String name);

  /**
   * 更新评估器最新版本
   * 迁移对应关系: Go语言EvaluatorDAOImpl.UpdateEvaluatorLatestVersion
   *
   * @param evaluatorId 评估器ID
   * @param version 版本号
   * @param userId 用户ID
   * @return 影响行数
   */
  int updateEvaluatorLatestVersion(@Param("evaluatorId") Long evaluatorId,
                                   @Param("version") String version,
                                   @Param("userId") String userId);
}
