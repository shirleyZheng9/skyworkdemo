package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemTurnID;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import java.util.List;
import java.util.Map;

/**
 * 实验轮次结果仓库接口
 * 对应Go: IExptTurnResultRepo
 */
public interface IExptTurnResultRepo {

  /**
   * 列出轮次结果
   * 对应Go: ListTurnResult(ctx context.Context, spaceID, exptID int64, filter *entity.ExptTurnResultFilter, page entity.Page, desc bool) ([]*entity.ExptTurnResult, int64, error)
   */
  List<ExptTurnResult> listTurnResult(Long spaceId, Long exptId, ExptTurnResultFilter filter, Page page, Boolean desc);

  /**
   * 根据项目ID列出轮次结果
   * 对应Go: ListTurnResultByItemIDs(ctx context.Context, spaceID, exptID int64, itemIDs []int64, page entity.Page, desc bool) ([]*entity.ExptTurnResult, int64, error)
   */
  List<ExptTurnResult> listTurnResultByItemIds(Long spaceId, Long exptId, List<Long> itemIds, ExptTurnResultFilter filter, Page page, Boolean desc);

  /**
   * 批量获取轮次结果
   * 对应Go: BatchGet(ctx context.Context, spaceID, exptID int64, itemIDs []int64) ([]*entity.ExptTurnResult, error)
   */
  List<ExptTurnResult> batchGet(Long spaceId, Long exptId, List<Long> itemIds);

  /**
   * 创建轮次评估器引用
   * 对应Go: CreateTurnEvaluatorRefs(ctx context.Context, turnResults []*entity.ExptTurnEvaluatorResultRef) error
   */
  void createTurnEvaluatorRefs(List<ExptTurnEvaluatorResultRef> turnResults);

  /**
   * 批量创建轮次结果（如果不存在）
   * 对应Go: BatchCreateNX(ctx context.Context, turnResults []*entity.ExptTurnResult) error
   */
  void batchCreateNx(List<ExptTurnResult> turnResults);

  /**
   * 获取项目轮次结果
   * 对应Go: GetItemTurnResults(ctx context.Context, exptID, itemID, spaceID int64) ([]*entity.ExptTurnResult, error)
   */
  List<ExptTurnResult> getItemTurnResults(Long exptId, Long itemId, Long spaceId);

  /**
   * 获取项目轮次结果（带exptRunId过滤）
   * 对应Go: GetItemTurnResults(ctx context.Context, exptID, itemID, spaceID int64) ([]*entity.ExptTurnResult, error)
   */
  List<ExptTurnResult> getItemTurnResults(Long exptId, Long itemId, Long spaceId, Long exptRunId);

  /**
   * 保存轮次结果
   * 对应Go: SaveTurnResults(ctx context.Context, turnResults []*entity.ExptTurnResult) error
   */
  void saveTurnResults(List<ExptTurnResult> turnResults);

  /**
   * 扫描轮次结果
   * 对应Go: ScanTurnResults(ctx context.Context, exptID int64, status []int32, cursor, limit, spaceID int64) ([]*entity.ExptTurnResult, int64, error)
   */
  PageInfo<ExptTurnResult> scanTurnResults(Long exptId, List<Integer> status, int pageNumber, Long pageSize, Long spaceId);

  /**
   * 更新轮次结果
   * 对应Go: UpdateTurnResults(ctx context.Context, exptID int64, itemTurnIDs []*entity.ItemTurnID, spaceID int64, ufields map[string]any) error
   */
  void updateTurnResults(Long exptId, List<ItemTurnID> itemTurnIds, Long spaceId, Map<String, Object> ufields);

  /**
   * 根据项目ID更新轮次结果
   * 对应Go: UpdateTurnResultsWithItemIDs(ctx context.Context, exptID int64, itemIDs []int64, spaceID int64, ufields map[string]any) error
   */
  void updateTurnResultsWithItemIds(Long exptId, List<Long> itemIds, Long spaceId, Map<String, Object> ufields);

  /**
   * 批量创建轮次运行日志（如果不存在）
   * 对应Go: BatchCreateNXRunLog(ctx context.Context, turnResults []*entity.ExptTurnResultRunLog) error
   */
  void batchCreateNxRunLog(List<ExptTurnResultRunLog> turnResults);

  /**
   * 获取项目轮次运行日志
   * 对应Go: GetItemTurnRunLogs(ctx context.Context, exptID, exptRunID, itemID, spaceID int64) ([]*entity.ExptTurnResultRunLog, error)
   */
  List<ExptTurnResultRunLog> getItemTurnRunLogs(Long exptId, Long exptRunId, Long itemId, Long spaceId);

  /**
   * 批量获取项目轮次运行日志
   * 对应Go: MGetItemTurnRunLogs(ctx context.Context, exptID, exptRunID int64, itemIDs []int64, spaceID int64) ([]*entity.ExptTurnResultRunLog, error)
   */
  List<ExptTurnResultRunLog> mGetItemTurnRunLogs(Long exptId, Long exptRunId, List<Long> itemIds, Long spaceId);

  /**
   * 保存轮次运行日志
   * 对应Go: SaveTurnRunLogs(ctx context.Context, turnResults []*entity.ExptTurnResultRunLog) error
   */
  void saveTurnRunLogs(List<ExptTurnResultRunLog> turnResults);

  /**
   * 创建或更新项目轮次运行日志状态
   * 对应Go: CreateOrUpdateItemsTurnRunLogStatus(ctx context.Context, spaceID, exptID, exptRunID int64, itemIDs []int64, status entity.TurnRunState) error
   */
  void createOrUpdateItemsTurnRunLogStatus(Long spaceId, Long exptId, Long exptRunId, List<Long> itemIds, TurnRunState status);

  /**
   * 扫描轮次运行日志
   * 对应Go: ScanTurnRunLogs(ctx context.Context, exptID, cursor, limit, spaceID int64) ([]*entity.ExptTurnResultRunLog, int64, error)
   */
  List<ExptTurnResultRunLog> scanTurnRunLogs(Long exptId, Long cursor, Long limit, Long spaceId);

  /**
   * 批量获取轮次评估器结果引用
   * 对应Go: BatchGetTurnEvaluatorResultRef(ctx context.Context, spaceID int64, exptTurnResultIDs []int64) ([]*entity.ExptTurnEvaluatorResultRef, error)
   */
  List<ExptTurnEvaluatorResultRef> batchGetTurnEvaluatorResultRef(Long spaceId, List<Long> exptTurnResultIds);

  /**
   * 根据实验ID获取轮次评估器结果引用
   * 对应Go: GetTurnEvaluatorResultRefByExptID(ctx context.Context, spaceID, exptID int64) ([]*entity.ExptTurnEvaluatorResultRef, error)
   */
  List<ExptTurnEvaluatorResultRef> getTurnEvaluatorResultRefByExptId(Long spaceId, Long exptId);

  /**
   * 根据评估器版本ID获取轮次评估器结果引用
   * 对应Go: GetTurnEvaluatorResultRefByEvaluatorVersionID(ctx context.Context, spaceID, exptID, evaluatorVersionID int64) ([]*entity.ExptTurnEvaluatorResultRef, error)
   */
  List<ExptTurnEvaluatorResultRef> getTurnEvaluatorResultRefByEvaluatorVersionId(Long spaceId, Long exptId, Long evaluatorVersionId);
}
