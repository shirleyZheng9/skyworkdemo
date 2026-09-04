package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新Prompt参数
 * 迁移对应关系: Go语言repo.UpdatePromptParam
 * - 功能: 定义更新Prompt的参数
 * - 字段定义:
 * * PromptID: int64 - Prompt ID
 * * UpdatedBy: string - 更新者
 * * PromptName: string - Prompt名称
 * * PromptDescription: string - Prompt描述
 * <p>
 * Java实现说明:
 * - 对应Go的repo.UpdatePromptParam结构体
 * - 使用Java类定义，包含更新参数字段
 * - 使用Lombok注解简化代码
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePromptParam {

  /**
   * Prompt ID
   * 迁移对应关系: Go语言repo.UpdatePromptParam.PromptID (int64)
   * - 功能: 要更新的Prompt标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 定位要更新的Prompt
   */
  private Long promptId;

  /**
   * 更新者
   * 迁移对应关系: Go语言repo.UpdatePromptParam.UpdatedBy (string)
   * - 功能: 执行更新的用户标识
   * - 类型: Go的string对应Java的String
   * - 用途: 审计和权限控制
   */
  private String updatedBy;

  /**
   * Prompt名称
   * 迁移对应关系: Go语言repo.UpdatePromptParam.PromptName (string)
   * - 功能: 新的Prompt名称
   * - 类型: Go的string对应Java的String
   * - 用途: 更新显示名称
   */
  private String promptName;

  /**
   * Prompt描述
   * 迁移对应关系: Go语言repo.UpdatePromptParam.PromptDescription (string)
   * - 功能: 新的Prompt描述
   * - 类型: Go的string对应Java的String
   * - 用途: 更新描述信息
   */
  private String promptDescription;
  @Schema(description = "目录ID")
  private Long catalogItemId;

  /**
   * 空间ID
   * - 功能: 空间标识
   * - 类型: Long
   * - 用途: 双主键查询，用于锁定唯一数据
   */
  private Long spaceId;

  /**
   * Prompt类型
   * - 功能: Prompt类型标识
   * - 类型: String
   * - 用途: 更新Prompt类型
   */
  private String promptType;
}
