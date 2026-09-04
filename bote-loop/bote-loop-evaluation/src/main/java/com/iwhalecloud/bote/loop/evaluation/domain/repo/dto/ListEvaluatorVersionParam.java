package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列出评估器版本请求
 * 对应Go: ListEvaluatorVersionRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluatorVersionParam {

  /**
   * 页面大小
   * 对应Go: PageSize int32
   */
  private Integer pageSize;

  /**
   * 页码
   * 对应Go: PageNum int32
   */
  private Integer pageNum;

  /**
   * 评估器ID
   * 对应Go: EvaluatorID int64
   */
  private Long evaluatorId;

  /**
   * 查询版本列表
   * 对应Go: QueryVersions []string
   */
  private List<String> queryVersions;

  /**
   * 排序条件
   * 对应Go: OrderBy []*entity.OrderBy
   */
  private List<OrderBy> orderBy;
}
