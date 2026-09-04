package com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto;

import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 条目快照列表查询参数
 * 迁移对应关系: Go语言ListItemSnapshotsParams
 * - 功能: 条目快照列表查询参数
 * - 字段定义: 分页、过滤条件等
 * <p>
 * Java实现说明:
 * - 对应Go的ListItemSnapshotsParams结构体
 * - 使用Java类定义，包含查询参数
 * - 提供参数验证功能
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go验证标签 -> Java验证注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListItemSnapshotsParams {

  /**
   * 分页器
   * 迁移对应关系: Go语言ListItemSnapshotsParams.Paginator
   * - 功能: 分页参数
   * - 类型: Go的*pagination.Paginator对应Java的Paginator
   * - 用途: 分页查询
   */
  private Paginator paginator;

  /**
   * 空间ID
   * 迁移对应关系: Go语言ListItemSnapshotsParams.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 验证: 必填且大于0
   * - 用途: 空间过滤
   */
  @NotNull(message = "spaceId is required")
  @Positive(message = "spaceId must be positive")
  private Long spaceId;

  /**
   * 版本ID（索引字段，必须设置）
   * 迁移对应关系: Go语言ListItemSnapshotsParams.VersionID
   * - 功能: 版本标识，索引字段
   * - 类型: Go的int64对应Java的Long
   * - 验证: 必填且大于0
   * - 用途: 版本过滤
   */
  @NotNull(message = "versionId is required")
  @Positive(message = "versionId must be positive")
  private Long versionId;
}
