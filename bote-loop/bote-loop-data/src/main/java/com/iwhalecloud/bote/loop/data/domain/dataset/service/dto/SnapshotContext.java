package com.iwhalecloud.bote.loop.data.domain.dataset.service.dto;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 快照上下文
 * 迁移对应关系: Go语言snapshotContext
 * - 功能: 管理快照处理上下文信息
 * - 字段定义: 各种快照处理状态字段
 * <p>
 * Java实现说明:
 * - 对应Go的snapshotContext结构体
 * - 使用Lombok注解简化代码
 * - 提供快照上下文管理功能
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go指针 -> Java包装类型
 * - Go布尔值 -> Java布尔值
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SnapshotContext {

  private Long spaceID;
  private Long versionID;
  private Long nowRetryTimes;
  private Boolean isFinished;
  private DatasetVersion version;
}
