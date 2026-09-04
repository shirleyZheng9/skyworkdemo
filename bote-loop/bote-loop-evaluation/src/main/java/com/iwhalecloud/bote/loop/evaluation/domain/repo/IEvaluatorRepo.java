package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorResponse;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ListEvaluatorVersionResponse;
import java.util.List;

/**
 * 定义 Evaluator 的 Repo 接口
 * 对应Go: IEvaluatorRepo
 */
public interface IEvaluatorRepo {

  /**
   * 创建评估器
   * 对应Go: CreateEvaluator(ctx context.Context, evaluator *entity.Evaluator) (evaluatorID int64, err error)
   */
  Long createEvaluator(Evaluator evaluator);

  /**
   * 提交评估器版本
   * 对应Go: SubmitEvaluatorVersion(ctx context.Context, evaluatorVersionDO *entity.Evaluator) error
   */
  void submitEvaluatorVersion(Evaluator evaluatorVersionDO);

  /**
   * 批量删除评估器
   * 对应Go: BatchDeleteEvaluator(ctx context.Context, ids []int64, userID string) error
   */
  void batchDeleteEvaluator(List<Long> ids, String userId);

  /**
   * 更新评估器草稿
   * 对应Go: UpdateEvaluatorDraft(ctx context.Context, version *entity.Evaluator) error
   */
  void updateEvaluatorDraft(Evaluator version);

  /**
   * 更新评估器元信息
   * 对应Go: UpdateEvaluatorMeta(ctx context.Context, id int64, name, description, userID string) error
   */
  void updateEvaluatorMeta(Long id, String name, String description, String userId, Long catalogItemId);

  /**
   * 根据ID批量获取评估器元信息
   * 对应Go: BatchGetEvaluatorMetaByID(ctx context.Context, ids []int64, includeDeleted bool) ([]*entity.Evaluator, error)
   */
  List<Evaluator> batchGetEvaluatorMetaById(List<Long> ids, Boolean includeDeleted);

  /**
   * 根据版本ID批量获取评估器
   * 对应Go: BatchGetEvaluatorByVersionID(ctx context.Context, spaceID *int64, ids []int64, includeDeleted bool) ([]*entity.Evaluator, error)
   */
  List<Evaluator> batchGetEvaluatorByVersionId(Long spaceId, List<Long> ids, Boolean includeDeleted);

  /**
   * 根据评估器ID批量获取评估器草稿
   * 对应Go: BatchGetEvaluatorDraftByEvaluatorID(ctx context.Context, spaceID int64, ids []int64, includeDeleted bool) ([]*entity.Evaluator, error)
   */
  List<Evaluator> batchGetEvaluatorDraftByEvaluatorId(Long spaceId, List<Long> ids, Boolean includeDeleted);

  /**
   * 根据评估器ID批量获取评估器版本
   * 对应Go: BatchGetEvaluatorVersionsByEvaluatorIDs(ctx context.Context, evaluatorIDs []int64, includeDeleted bool) ([]*entity.Evaluator, error)
   */
  List<Evaluator> batchGetEvaluatorVersionsByEvaluatorIds(List<Long> evaluatorIds, Boolean includeDeleted);

  /**
   * 列出评估器
   * 对应Go: ListEvaluator(ctx context.Context, req *ListEvaluatorRequest) (*ListEvaluatorResponse, error)
   */
  ListEvaluatorResponse listEvaluator(ListEvaluatorParam request);

  /**
   * 列出评估器版本
   * 对应Go: ListEvaluatorVersion(ctx context.Context, req *ListEvaluatorVersionRequest) (*ListEvaluatorVersionResponse, error)
   */
  ListEvaluatorVersionResponse listEvaluatorVersion(ListEvaluatorVersionParam request);

  /**
   * 检查名称是否存在
   * 对应Go: CheckNameExist(ctx context.Context, spaceID, evaluatorID int64, name string) (bool, error)
   */
  Boolean checkNameExist(Long spaceId, Long evaluatorId, String name);

  /**
   * 检查版本是否存在
   * 对应Go: CheckVersionExist(ctx context.Context, evaluatorID int64, version string) (bool, error)
   */
  Boolean checkVersionExist(Long evaluatorId, String version);
}
