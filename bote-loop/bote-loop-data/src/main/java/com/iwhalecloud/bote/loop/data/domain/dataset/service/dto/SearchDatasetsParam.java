package com.iwhalecloud.bote.loop.data.domain.dataset.service.dto;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetCategory;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索数据集参数
 * 迁移对应关系: Go语言service.SearchDatasetsParam
 * - 功能: 搜索数据集的查询参数对象
 * - 字段定义: 各种搜索条件和分页参数
 * <p>
 * Java实现说明:
 * - 对应Go的service.SearchDatasetsParam结构体
 * - 使用Lombok注解简化代码
 * - 提供数据集搜索所需的参数
 * - 支持Builder模式构建对象
 * - 支持有参和无参构造器
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片 -> Java List
 * - Go指针 -> Java包装类型
 * - Go枚举 -> Java枚举
 * - Go整数 -> Java Long/Integer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDatasetsParam {

  /**
   * 空间ID
   * 迁移对应关系: Go语言SearchDatasetsParam.SpaceID
   * - 功能: 标识数据集所属的空间
   * - 类型: int64 -> Long
   * - 用途: 空间隔离
   */
  private Long spaceId;

  /**
   * 数据集ID列表
   * 迁移对应关系: Go语言SearchDatasetsParam.DatasetIDs
   * - 功能: 指定要搜索的数据集ID列表
   * - 类型: []int64 -> List<Long>
   * - 用途: 精确匹配数据集ID
   */
  private List<Long> datasetIds;

  /**
   * 数据集分类
   * 迁移对应关系: Go语言SearchDatasetsParam.Category
   * - 功能: 按分类筛选数据集
   * - 类型: entity.DatasetCategory -> DatasetCategory
   * - 用途: 分类筛选
   */
  private DatasetCategory category;

  /**
   * 数据集名称
   * 迁移对应关系: Go语言SearchDatasetsParam.Name
   * - 功能: 支持模糊搜索的数据集名称
   * - 类型: *string -> String
   * - 用途: 名称模糊匹配
   * - 注意: 可为空，表示不按名称筛选
   */
  private String name;

  /**
   * 创建者列表
   * 迁移对应关系: Go语言SearchDatasetsParam.CreatedBys
   * - 功能: 按创建者筛选数据集
   * - 类型: []string -> List<String>
   * - 用途: 创建者筛选
   */
  private List<String> createdBys;
  private String catalogItemId;

  /**
   * 页码
   * 迁移对应关系: Go语言SearchDatasetsParam.Page
   * - 功能: 分页页码
   * - 类型: *int32 -> Integer
   * - 用途: 分页控制
   * - 注意: 可为空，与cursor同时提供时优先使用cursor
   */
  private Integer page;

  /**
   * 分页大小
   * 迁移对应关系: Go语言SearchDatasetsParam.PageSize
   * - 功能: 每页数据条数
   * - 类型: *int32 -> Integer
   * - 用途: 分页控制
   * - 注意: 范围(0, 200]，默认为20
   */
  private Integer pageSize;
  private Integer pageNumber;

  /**
   * 游标
   * 迁移对应关系: Go语言SearchDatasetsParam.Cursor
   * - 功能: 分页游标
   * - 类型: *string -> String
   * - 用途: 分页导航
   * - 注意: 与page同时提供时优先使用cursor
   */
  private String cursor;

  /**
   * 排序规则
   * 迁移对应关系: Go语言SearchDatasetsParam.OrderBy
   * - 功能: 结果排序规则
   * - 类型: *OrderBy -> OrderBy
   * - 用途: 结果排序
   * - 注意: 可为空，使用默认排序
   */
  private OrderBy orderBy;

  /**
   * 业务分类列表
   * 迁移对应关系: Go语言SearchDatasetsParam.BizCategorys
   * - 功能: 按业务分类筛选数据集
   * - 类型: []string -> List<String>
   * - 用途: 业务分类筛选
   */
  private List<String> bizCategorys;
}
