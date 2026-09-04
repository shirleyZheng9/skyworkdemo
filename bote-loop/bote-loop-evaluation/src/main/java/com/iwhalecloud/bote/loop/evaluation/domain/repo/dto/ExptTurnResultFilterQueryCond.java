package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果过滤器查询条件实体
 * 迁移对应关系: Go语言ExptTurnResultFilterQueryCond
 * - 功能: 实验轮次结果过滤器查询条件
 * - 字段: 主表字段、map字段、联表、全文搜索、分页等
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResultFilterQueryCond结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *string -> Java String
 * - Go []*FieldFilter -> Java List<FieldFilter>
 * - Go *time.Time -> Java LocalDateTime
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResultFilterQueryCond {
  // 主表字段
  @JsonProperty("space_id")
  private String spaceId;

  @JsonProperty("expt_id")
  private String exptId;

  @JsonProperty("item_ids")
  private List<FieldFilter> itemIds;

  @JsonProperty("item_run_status")
  private List<FieldFilter> itemRunStatus;

  @JsonProperty("turn_run_status")
  private List<FieldFilter> turnRunStatus;

  @JsonProperty("evaluator_score_corrected")
  private FieldFilter evaluatorScoreCorrected;

  @JsonProperty("created_date")
  private LocalDateTime createdDate;

  @JsonProperty("eval_set_version_id")
  private String evalSetVersionId;

  // 主表map字段
  @JsonProperty("map_cond")
  private ExptTurnResultFilterMapCond mapCond;

  // 联表
  @JsonProperty("item_snapshot_cond")
  private ItemSnapshotFilter itemSnapshotCond;

  @JsonProperty("eval_set_sync_ck_date")
  private String evalSetSyncCkDate;

  // 全文搜索
  @JsonProperty("keyword_search")
  private KeywordMapCond keywordSearch;

  // 分页
  @JsonProperty("page")
  private Page page;
}
