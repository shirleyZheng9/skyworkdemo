package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt基础信息实体
 * 迁移对应关系: Go语言entity.PromptBasic
 * - 功能: 存储Prompt的基础元数据信息
 * - 字段定义:
 * * DisplayName: string - 显示名称
 * * Description: string - 描述
 * * LatestVersion: string - 最新版本
 * * CreatedBy: string - 创建者
 * * UpdatedBy: string - 更新者
 * * CreatedAt: time.Time - 创建时间
 * * UpdatedAt: time.Time - 更新时间
 * * LatestCommittedAt: *time.Time - 最新提交时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.PromptBasic结构体
 * - 使用Java类定义，包含所有基础信息字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 时间类型使用LocalDateTime替代Go的time.Time
 * - 可空时间字段使用LocalDateTime类型
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go time.Time -> Java LocalDateTime
 * - Go *time.Time -> Java LocalDateTime (可空)
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptBasic {

  /**
   * 显示名称
   * 迁移对应关系: Go语言entity.PromptBasic.DisplayName (string)
   * - 功能: Prompt的显示名称
   * - 类型: Go的string对应Java的String
   * - 用途: 用户界面显示和识别
   */
  @JsonProperty("display_name")
  private String displayName;

  /**
   * 描述
   * 迁移对应关系: Go语言entity.PromptBasic.Description (string)
   * - 功能: Prompt的描述信息
   * - 类型: Go的string对应Java的String
   * - 用途: 说明Prompt的用途和功能
   */
  @JsonProperty("description")
  private String description;

  /**
   * 最新版本
   * 迁移对应关系: Go语言entity.PromptBasic.LatestVersion (string)
   * - 功能: Prompt的最新版本号
   * - 类型: Go的string对应Java的String
   * - 用途: 版本管理和追踪
   */
  @JsonProperty("latest_version")
  private String latestVersion;

  /**
   * 创建者
   * 迁移对应关系: Go语言entity.PromptBasic.CreatedBy (string)
   * - 功能: Prompt的创建者用户标识
   * - 类型: Go的string对应Java的String
   * - 用途: 审计和权限控制
   */
  @JsonProperty("created_by")
  private String createdBy;

  /**
   * 更新者
   * 迁移对应关系: Go语言entity.PromptBasic.UpdatedBy (string)
   * - 功能: Prompt的最后更新者用户标识
   * - 类型: Go的string对应Java的String
   * - 用途: 审计和权限控制
   */
  @JsonProperty("updated_by")
  private String updatedBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言entity.PromptBasic.CreatedAt (time.Time)
   * - 功能: Prompt的创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 用途: 审计和时间追踪
   */
  @JsonProperty("created_at")
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言entity.PromptBasic.UpdatedAt (time.Time)
   * - 功能: Prompt的最后更新时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 用途: 审计和时间追踪
   */
  @JsonProperty("updated_at")
  private Date updatedAt;

  /**
   * 最新提交时间
   * 迁移对应关系: Go语言entity.PromptBasic.LatestCommittedAt (*time.Time)
   * - 功能: Prompt的最新提交时间
   * - 类型: Go的*time.Time对应Java的LocalDateTime (可空)
   * - 用途: 追踪最新提交状态
   * - 注意: 在Go中为指针类型，在Java中为可空对象
   */
  @JsonProperty("latest_committed_at")
  private Date latestCommittedAt;

  /**
   * Prompt类型
   * - 功能: Prompt类型标识
   * - 类型: PromptType枚举
   * - 枚举值: SystemPrompt（系统提示词）、QuestionClassifier（问题分类）、ParamExtractor（参数提取）
   */
  @JsonProperty("prompt_type")
  private PromptType promptType;

}
