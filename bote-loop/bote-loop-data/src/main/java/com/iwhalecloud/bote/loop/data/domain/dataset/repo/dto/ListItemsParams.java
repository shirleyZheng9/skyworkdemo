package com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto;

import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集项目列表查询参数
 * 迁移对应关系: Go语言ListItemsParams
 * - 功能: 数据集项目列表查询参数
 * - 字段定义: 分页、过滤条件等
 * <p>
 * Java实现说明:
 * - 对应Go的ListItemsParams结构体
 * - 使用Java类定义，包含查询参数
 * - 提供参数验证功能
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片 -> Java列表
 * - Go验证标签 -> Java验证注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListItemsParams {

  /**
   * 分页器
   * 迁移对应关系: Go语言ListItemsParams.Paginator
   * - 功能: 分页参数
   * - 类型: Go的*pagination.Paginator对应Java的Paginator
   * - 用途: 分页查询
   */
  private Paginator paginator;

  /**
   * 空间ID（分片键）
   * 迁移对应关系: Go语言ListItemsParams.SpaceID
   * - 功能: 空间标识，分片键
   * - 类型: Go的int64对应Java的Long
   * - 验证: 必填且大于0
   * - 用途: 分片查询
   */
  @NotNull(message = "spaceId is required")
  @Positive(message = "spaceId must be positive")
  private Long spaceId;

  /**
   * 数据集ID
   * 迁移对应关系: Go语言ListItemsParams.DatasetID
   * - 功能: 数据集标识
   * - 类型: Go的int64对应Java的Long
   * - 验证: 必填且大于0
   * - 用途: 数据集过滤
   */
  @NotNull(message = "datasetId is required")
  @Positive(message = "datasetId must be positive")
  private Long datasetId;

  /**
   * 项目键列表
   * 迁移对应关系: Go语言ListItemsParams.ItemKeys
   * - 功能: 项目键列表
   * - 类型: Go的[]string对应Java的List<String>
   * - 用途: 按项目键过滤
   */
  private List<String> itemKeys;

  /**
   * 项目ID列表
   * 迁移对应关系: Go语言ListItemsParams.ItemIDs
   * - 功能: 项目ID列表
   * - 类型: Go的[]int64对应Java的List<Long>
   * - 用途: 按项目ID过滤
   */
  private List<Long> itemIds;

  /**
   * 添加版本号等于
   * 迁移对应关系: Go语言ListItemsParams.AddVNEq
   * - 功能: 添加版本号等于指定值
   * - 类型: Go的int64对应Java的Long
   * - 用途: 版本过滤
   */
  private Long addVnEq;

  /**
   * 删除版本号等于
   * 迁移对应关系: Go语言ListItemsParams.DelVNEq
   * - 功能: 删除版本号等于指定值
   * - 类型: Go的int64对应Java的Long
   * - 用途: 版本过滤
   */
  private Long delVnEq;

  /**
   * 添加版本号小于等于
   * 迁移对应关系: Go语言ListItemsParams.AddVNLte
   * - 功能: 添加版本号小于等于指定值
   * - 类型: Go的int64对应Java的Long
   * - 用途: 版本范围过滤
   */
  private Long addVnLte;

  /**
   * 删除版本号大于
   * 迁移对应关系: Go语言ListItemsParams.DelVNGt
   * - 功能: 删除版本号大于指定值
   * - 类型: Go的int64对应Java的Long
   * - 用途: 版本范围过滤
   */
  private Long delVnGt;

  /**
   * 项目ID大于
   * 迁移对应关系: Go语言ListItemsParams.ItemIDGt
   * - 功能: 项目ID大于指定值
   * - 类型: Go的int64对应Java的Long
   * - 用途: ID范围过滤
   */
  private Long itemIdGt;
}
