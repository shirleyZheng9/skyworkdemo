package com.iwhalecloud.bote.loop.evaluation.domain.repo;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemRunLogFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import java.util.List;
import java.util.Map;

/**
 * 实验项目结果仓库接口
 * 对应Go: IExptItemResultRepo
 */
public interface IExptItemResultRepo {

  /**
   * 批量获取项目结果
   * 对应Go: BatchGet(ctx context.Context, spaceID, exptID int64, itemIDs []int64) ([]*entity.ExptItemResult, error)
   */
  List<ExptItemResult> batchGet(Long spaceId, Long exptId, List<Long> itemIds);

  /**
   * 批量创建项目结果（如果不存在）
   * 对应Go: BatchCreateNX(ctx context.Context, itemResults []*entity.ExptItemResult) error
   */
  void batchCreateNx(List<ExptItemResult> itemResults);

  /**
   * 扫描项目结果
   * 对应Go: ScanItemResults(ctx context.Context, exptID, cursor, limit int64, status []int32, spaceID int64) (results []*entity.ExptItemResult, ncursor int64, err error)
   */
  List<ExptItemResult> scanItemResults(Long exptId, Long cursor, Long limit, List<Integer> status, Long spaceId);

  /**
   * 根据实验ID列出项目结果
   * 对应Go: ListItemResultsByExptID(ctx context.Context, exptID, spaceID int64, page entity.Page, desc bool) ([]*entity.ExptItemResult, int64, error)
   */
  PageInfo<ExptItemResult> listItemResultsByExptId(Long exptId, Long spaceId, Page page, Boolean desc);

  /**
   * 根据实验ID获取项目ID列表
   * 对应Go: GetItemIDListByExptID(ctx context.Context, exptID, spaceID int64) (itemIDList []int64, err error)
   */
  List<Long> getItemIdListByExptId(Long exptId, Long spaceId);

  /**
   * 保存项目结果
   * 对应Go: SaveItemResults(ctx context.Context, itemResults []*entity.ExptItemResult) error
   */
  void saveItemResults(List<ExptItemResult> itemResults);

  /**
   * 获取项目轮次结果
   * 对应Go: GetItemTurnResults(ctx context.Context, spaceID, exptID, itemID int64) ([]*entity.ExptTurnResult, error)
   */
  List<ExptTurnResult> getItemTurnResults(Long spaceId, Long exptId, Long itemId);

  /**
   * 更新项目结果
   * 对应Go: UpdateItemsResult(ctx context.Context, spaceID, exptID int64, itemID []int64, ufields map[string]any) error
   */
  void updateItemsResult(Long spaceId, Long exptId, List<Long> itemIds, Map<String, Object> ufields);

  /**
   * 根据实验ID获取最大项目索引
   * 对应Go: GetMaxItemIdxByExptID(ctx context.Context, exptID, spaceID int64) (int32, error)
   */
  Integer getMaxItemIdxByExptId(Long exptId, Long spaceId);

  /**
   * 批量创建项目运行日志（如果不存在）
   * 对应Go: BatchCreateNXRunLogs(ctx context.Context, itemRunLogs []*entity.ExptItemResultRunLog) error
   */
  void batchCreateNxRunLogs(List<ExptItemResultRunLog> itemRunLogs);

  /**
   * 扫描项目运行日志
   * 对应Go: ScanItemRunLogs(ctx context.Context, exptID, exptRunID int64, filter *entity.ExptItemRunLogFilter, cursor, limit, spaceID int64) ([]*entity.ExptItemResultRunLog, int64, error)
   */
  List<ExptItemResultRunLog> scanItemRunLogs(Long exptId, Long exptRunId, ExptItemRunLogFilter filter, Long cursor, Long limit, Long spaceId);

  /**
   * 更新项目运行日志
   * 对应Go: UpdateItemRunLog(ctx context.Context, exptID, exptRunID int64, itemID []int64, ufields map[string]any, spaceID int64) error
   */
  void updateItemRunLog(Long exptId, Long exptRunId, List<Long> itemIds, Map<String, Object> ufields, Long spaceId);

  /**
   * 获取项目运行日志
   * 对应Go: GetItemRunLog(ctx context.Context, exptID, exptRunID, itemID, spaceID int64) (*entity.ExptItemResultRunLog, error)
   */
  ExptItemResultRunLog getItemRunLog(Long exptId, Long exptRunId, Long itemId, Long spaceId);

  /**
   * 批量获取项目运行日志
   * 对应Go: MGetItemRunLog(ctx context.Context, exptID, exptRunID int64, itemIDs []int64, spaceID int64) ([]*entity.ExptItemResultRunLog, error)
   */
  List<ExptItemResultRunLog> mGetItemRunLog(Long exptId, Long exptRunId, List<Long> itemIds, Long spaceId);
}
