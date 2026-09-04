package com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetCategory;
import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.session.RowBounds;

/**
 * 数据集列表查询参数
 * 迁移对应关系: Go语言ListDatasetsParams
 * - 功能: 数据集列表查询参数
 * - 字段定义: 分页、过滤条件等
 * <p>
 * Java实现说明:
 * - 对应Go的ListDatasetsParams结构体
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
public class ListDatasetsParams {

  /**
   * 分页器
   * 迁移对应关系: Go语言ListDatasetsParams.Paginator
   * - 功能: 分页参数
   * - 类型: Go的*pagination.Paginator对应Java的Paginator
   * - 用途: 分页查询
   */
  private Paginator paginator;

  /**
   * 空间ID（分片键）
   * 迁移对应关系: Go语言ListDatasetsParams.SpaceID
   * - 功能: 空间标识，分片键
   * - 类型: Go的int64对应Java的Long
   * - 验证: 必填且大于0
   * - 用途: 分片查询
   */
  @NotNull(message = "spaceId is required")
  @Positive(message = "spaceId must be positive")
  private Long spaceId;

  /**
   * 数据集ID列表
   * 迁移对应关系: Go语言ListDatasetsParams.IDs
   * - 功能: 数据集ID列表
   * - 类型: Go的[]int64对应Java的List<Long>
   * - 用途: 批量查询
   */
  private List<Long> ids;

  /**
   * 业务场景分类
   * 迁移对应关系: Go语言ListDatasetsParams.Category
   * - 功能: 业务场景分类
   * - 类型: Go的string对应Java的String
   * - 用途: 分类过滤
   */
  private DatasetCategory category;

  /**
   * 创建人列表
   * 迁移对应关系: Go语言ListDatasetsParams.CreatedBys
   * - 功能: 创建人列表
   * - 类型: Go的[]string对应Java的List<String>
   * - 用途: 创建人过滤
   */
  private List<String> createdBys;

  /**
   * 名称模糊搜索
   * 迁移对应关系: Go语言ListDatasetsParams.NameLike
   * - 功能: 按名称模糊搜索
   * - 类型: Go的string对应Java的String
   * - 用途: 名称模糊查询
   */
  private String nameLike;

  /**
   * 业务场景下自定义分类列表
   * 迁移对应关系: Go语言ListDatasetsParams.BizCategorys
   * - 功能: 业务场景下自定义分类列表
   * - 类型: Go的[]string对应Java的List<String>
   * - 用途: 自定义分类过滤
   */
  private List<String> bizCategorys;

  private Long catalogItemId;

  /**
   * 构造分页参数
   */
  public RowBounds buildRowBounds() {
    if (paginator == null) {
      return new RowBounds(0, 20);
    }
    int offset = paginator.getOffset() != null ? paginator.getOffset() : 0;
    int limit = paginator.getLimit() != null ? paginator.getLimit() : 20;
    return new RowBounds(offset, limit);
  }
}
