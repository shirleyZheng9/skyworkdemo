package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果过滤器加速器实体
 * 对应Go: entity.ExptTurnResultFilterAccelerator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResultFilterAccelerator {

  /**
   * 工作空间ID（必带字段）
   * 对应Go: SpaceID int64
   */
  private Long spaceId;

  /**
   * 实验ID（必带字段）
   * 对应Go: ExptID int64
   */
  private Long exptId;

  /**
   * 创建日期（必带字段）
   * 对应Go: CreatedDate time.Time
   */
  private LocalDateTime createdDate;

  /**
   * 评估器分数修正（基础查询）
   * 对应Go: EvaluatorScoreCorrected *FieldFilter
   */
  private FieldFilterDO evaluatorScoreCorrected;

  /**
   * 数据项ID过滤器列表（基础查询）
   * 对应Go: ItemIDs []*FieldFilter
   */
  private List<FieldFilterDO> itemIds;

  /**
   * 数据项运行状态过滤器列表（基础查询）
   * 对应Go: ItemRunStatus []*FieldFilter
   */
  private List<FieldFilterDO> itemRunStatus;

  /**
   * 轮次运行状态过滤器列表（基础查询）
   * 对应Go: TurnRunStatus []*FieldFilter
   */
  private List<FieldFilterDO> turnRunStatus;

  /**
   * Map类查询条件
   * 对应Go: MapCond *ExptTurnResultFilterMapCond
   */
  private ExptTurnResultFilterMapCond mapCond;

  /**
   * 数据项快照条件
   * 对应Go: ItemSnapshotCond *ItemSnapshotFilter
   */
  private ItemSnapshotFilterDO itemSnapshotCond;

  /**
   * 关键词搜索
   * 对应Go: KeywordSearch *KeywordFilter
   */
  private KeywordFilter keywordSearch;

  /**
   * 分页信息
   * 对应Go: Page Page
   */
  private Page page;

  /**
   * 评估集同步CK日期
   * 对应Go: EvalSetSyncCkDate string
   */
  private String evalSetSyncCkDate;
}
