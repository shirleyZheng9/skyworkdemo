package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调用实验请求
 * 对应Go: InvokeExptReq
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvokeExptReq {

  /**
   * 实验ID
   * 对应Go: ExptID int64
   */
  private Long exptId;

  /**
   * 运行ID
   * 对应Go: RunID int64
   */
  private Long runId;

  /**
   * 工作空间ID
   * 对应Go: SpaceID int64
   */
  private Long spaceId;

  /**
   * 会话信息
   * 对应Go: Session *entity.Session
   */
  private Session session;

  /**
   * 数据项列表
   * 对应Go: Items []*entity.EvaluationSetItem
   */
  private List<EvaluationSetItem> items;

  /**
   * 扩展信息
   * 对应Go: Ext map[string]string
   */
  private Map<String, String> ext;
}
