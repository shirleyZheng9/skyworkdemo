package com.iwhalecloud.bote.loop.prompt.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交信息实体
 * 迁移对应关系: Go语言entity.CommitInfo
 * - 功能: 存储提交的元数据信息
 * - 字段定义:
 * * Version: string - 版本号
 * * BaseVersion: string - 基础版本
 * * Description: string - 描述
 * * CommittedBy: string - 提交者
 * * CommittedAt: time.Time - 提交时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.CommitInfo结构体
 * - 使用Java类定义，包含getter/setter方法
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 时间类型使用LocalDateTime替代Go的time.Time
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go time.Time -> Java LocalDateTime
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitInfo {
  @Schema(description = "版本号")
  private String version;
  @Schema(description = "基础版本")
  private String baseVersion;
  @Schema(description = "描述")
  private String description;
  @Schema(description = "提交者")
  private String committedBy;
  @Schema(description = "提交时间")
  private Date committedAt;
}
