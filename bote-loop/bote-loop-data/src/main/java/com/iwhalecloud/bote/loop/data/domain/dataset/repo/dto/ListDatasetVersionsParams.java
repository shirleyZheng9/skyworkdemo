package com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto;

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
 * 数据集版本列表查询参数
 * 迁移对应关系: Go语言ListDatasetVersionsParams
 * - 功能: 数据集版本列表查询参数
 * - 字段定义: 分页、过滤条件等
 * <p>
 * Java实现说明:
 * - 对应Go的ListDatasetVersionsParams结构体
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
public class ListDatasetVersionsParams {

  /**
   * 分页器
   * 迁移对应关系: Go语言ListDatasetVersionsParams.Paginator
   * - 功能: 分页参数
   * - 类型: Go的*pagination.Paginator对应Java的Paginator
   * - 用途: 分页查询
   */
  private Paginator paginator;

  /**
   * 空间ID（分片键）
   * 迁移对应关系: Go语言ListDatasetVersionsParams.SpaceID
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
   * 迁移对应关系: Go语言ListDatasetVersionsParams.DatasetID
   * - 功能: 数据集标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 数据集过滤
   */
  private Long datasetId;

  /**
   * 版本ID列表
   * 迁移对应关系: Go语言ListDatasetVersionsParams.IDs
   * - 功能: 版本ID列表
   * - 类型: Go的[]int64对应Java的List<Long>
   * - 用途: 批量查询
   */
  private List<Long> ids;

  /**
   * 版本号列表
   * 迁移对应关系: Go语言ListDatasetVersionsParams.Versions
   * - 功能: 版本号列表
   * - 类型: Go的[]string对应Java的List<String>
   * - 用途: 版本号过滤
   */
  private List<String> versions;

  /**
   * 数字版本号列表
   * 迁移对应关系: Go语言ListDatasetVersionsParams.VersionNums
   * - 功能: 数字版本号列表
   * - 类型: Go的[]int64对应Java的List<Long>
   * - 用途: 数字版本号过滤
   */
  private List<Long> versionNums;

  /**
   * 版本号模糊搜索
   * 迁移对应关系: Go语言ListDatasetVersionsParams.VersionLike
   * - 功能: 按版本号模糊搜索
   * - 类型: Go的string对应Java的String
   * - 用途: 版本号模糊查询
   */
  private String versionLike;

  /**
   * 构造分页参数
   */
  public RowBounds buildRowBounds() {
    int offset = paginator == null || paginator.getOffset() == null ? 0 : paginator.getOffset();
    int limit = paginator == null || paginator.getLimit() == null ? 20 : paginator.getLimit();
    return new RowBounds(offset, limit);
  }
}
