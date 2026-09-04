package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询数据集项目参数
 * 迁移对应关系: Go语言ListDatasetItemsParam结构体
 * - 功能: 查询数据集项目列表的参数对象
 * - 字段定义:
 * * spaceId: Long - 空间ID
 * * evaluationSetId: Long - 评估集ID
 * * versionId: Long - 版本ID
 * * pageNumber: Integer - 页码
 * * pageSize: Integer - 页大小
 * * pageToken: String - 分页令牌
 * * orderBys: List<OrderBy> - 排序条件列表
 * * itemIdsNotIn: List<Long> - 排除的项目ID列表
 * <p>
 * Java实现说明:
 * - 对应Go的ListDatasetItemsParam结构体
 * - 使用Lombok注解简化代码
 * - 支持Builder模式
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go *int64 -> Java Long
 * - Go *int32 -> Java Integer
 * - Go *string -> Java String
 * - Go []int64 -> Java List<Long>
 * - Go []*entity.OrderBy -> Java List<OrderBy>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDatasetItemsParam {

  /**
   * 空间ID
   * 迁移对应关系: Go语言ListDatasetItemsParam.SpaceID
   */
  private Long spaceId;

  /**
   * 评估集ID
   * 迁移对应关系: Go语言ListDatasetItemsParam.EvaluationSetID
   */
  private Long evaluationSetId;

  /**
   * 版本ID
   * 迁移对应关系: Go语言ListDatasetItemsParam.VersionID
   */
  private Long versionId;

  /**
   * 页码
   * 迁移对应关系: Go语言ListDatasetItemsParam.PageNumber
   */
  private Integer pageNumber;

  /**
   * 页大小
   * 迁移对应关系: Go语言ListDatasetItemsParam.PageSize
   */
  private Integer pageSize;

  /**
   * 分页令牌
   * 迁移对应关系: Go语言ListDatasetItemsParam.PageToken
   */
  private String pageToken;

  /**
   * 排序条件列表
   * 迁移对应关系: Go语言ListDatasetItemsParam.OrderBys
   */
  private List<OrderBy> orderBys;

  /**
   * 排除的项目ID列表
   * 迁移对应关系: Go语言ListDatasetItemsParam.ItemIDsNotIn
   */
  private List<Long> itemIdsNotIn;
}
