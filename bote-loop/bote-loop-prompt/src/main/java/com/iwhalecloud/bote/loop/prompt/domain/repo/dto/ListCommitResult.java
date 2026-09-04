package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.iwhalecloud.bote.loop.prompt.domain.entity.CommitInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表查询提交信息结果
 * 迁移对应关系: Go语言repo.ListCommitResult
 * - 功能: 存储列表查询提交信息的结果
 * - 字段定义:
 * * CommitInfoDOs: []*entity.CommitInfo - 提交信息列表
 * * NextPageToken: int64 - 下一页令牌
 * <p>
 * Java实现说明:
 * - 对应Go的repo.ListCommitResult结构体
 * - 使用Java类定义，包含查询结果字段
 * - 使用Lombok注解简化代码
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []*entity.CommitInfo -> Java List<CommitInfo>
 * - Go int64 -> Java Long
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListCommitResult {

  /**
   * 提交信息列表
   * 迁移对应关系: Go语言repo.ListCommitResult.CommitInfoDOs ([]*entity.CommitInfo)
   * - 功能: 提交信息对象列表
   * - 类型: Go的[]*entity.CommitInfo对应Java的List<CommitInfo>
   * - 用途: 查询结果数据
   */
  private List<CommitInfo> commitInfoDOs;

  /**
   * 下一页令牌
   * 迁移对应关系: Go语言repo.ListCommitResult.NextPageToken (int64)
   * - 功能: 下一页查询的令牌
   * - 类型: Go的int64对应Java的Long
   * - 用途: 游标分页
   */
  private Long nextPageToken;

}
