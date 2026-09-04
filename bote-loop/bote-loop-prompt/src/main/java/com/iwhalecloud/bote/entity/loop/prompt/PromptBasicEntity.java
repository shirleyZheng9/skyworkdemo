package com.iwhalecloud.bote.entity.loop.prompt;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt基础信息持久化对象
 * 迁移对应关系: Go语言model.PromptBasic
 * - 功能: Prompt基础表的数据模型
 * - 表名: prompt_basic
 * - 字段定义:
 * * id: Long - 主键ID
 * * spaceId: Long - 空间ID
 * * promptKey: String - Prompt key
 * * name: String - Prompt名称
 * * description: String - 描述
 * * createdBy: String - 创建人
 * * updatedBy: String - 更新人
 * * latestVersion: String - 最新版本
 * * latestCommitTime: LocalDateTime - 最新提交时间
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.PromptBasic结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 支持软删除功能
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go soft_delete.DeletedAt -> Java Long
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bt_prompt_basic")
public class PromptBasicEntity {

  /**
   * 主键ID
   * 迁移对应关系: Go语言PromptBasic.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * 空间ID
   * 迁移对应关系: Go语言PromptBasic.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * Prompt key
   * 迁移对应关系: Go语言PromptBasic.PromptKey
   * - 功能: Prompt唯一标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String promptKey;

  /**
   * Prompt名称
   * 迁移对应关系: Go语言PromptBasic.Name
   * - 功能: Prompt显示名称
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String name;

  /**
   * 描述
   * 迁移对应关系: Go语言PromptBasic.Description
   * - 功能: Prompt描述信息
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(1024), not null
   */
  private String description;

  /**
   * 创建人
   * 迁移对应关系: Go语言PromptBasic.CreatedBy
   * - 功能: 创建者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 更新人
   * 迁移对应关系: Go语言PromptBasic.UpdatedBy
   * - 功能: 更新者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String updatedBy;

  /** 提交状态 */
  private Integer commitStatus;
  /**
   * 最新版本
   * 迁移对应关系: Go语言PromptBasic.LatestVersion
   * - 功能: 最新版本号
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String latestVersion;

  /**
   * 最新提交时间
   * 迁移对应关系: Go语言PromptBasic.LatestCommitTime
   * - 功能: 最新提交时间
   * - 类型: Go的*time.Time对应Java的Date
   * - 数据库: timestamptz
   */
  private Date latestCommitTime;

  /**
   * 创建时间
   * 迁移对应关系: Go语言PromptBasic.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言PromptBasic.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言PromptBasic.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: bigint(20), not null, default 0
   */
  private Long deletedAt;

  /** 目录ID */
  private Long catalogItemId;

  /**
   * Prompt类型
   * - 功能: Prompt类型标识
   * - 类型: String
   * - 数据库: varchar(64), nullable
   * - 枚举值: SystemPrompt（系统提示词）、QuestionClassifier（问题分类）、ParamExtractor（参数提取）
   */
  private String promptType;
}
