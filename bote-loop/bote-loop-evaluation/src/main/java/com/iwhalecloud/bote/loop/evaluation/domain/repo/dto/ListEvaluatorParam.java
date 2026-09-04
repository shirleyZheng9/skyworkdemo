package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列出评估器请求
 * 对应Go: ListEvaluatorRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluatorParam {

  /**
   * 空间ID
   * 对应Go: SpaceID int64
   */
  private Long spaceId;

  /**
   * 搜索名称
   * 对应Go: SearchName string
   */
  private String searchName;

  /**
   * 创建者ID列表
   * 对应Go: CreatorIDs []int64
   */
  private List<Long> creatorIds;

  /**
   * 评估器类型列表
   * 对应Go: EvaluatorType []entity.EvaluatorType
   */
  private List<EvaluatorType> evaluatorType;

  private Long catalogItemId;

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

  /** 是否具有版本 */
  private boolean withVersion;

  /**
   * 排序条件
   * 对应Go: OrderBy []*entity.OrderBy
   */
  private List<OrderBy> orderBy;
}
