package com.iwhalecloud.bote.entity.loop.prompt;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Prompt用户草稿持久化对象
 * 迁移对应关系: Go语言model.PromptUserDraft
 * - 功能: Draft表的数据模型
 * - 表名: prompt_user_draft
 * - 字段定义:
 * * id: Long - 主键ID
 * * spaceId: Long - 空间ID
 * * promptId: Long - Prompt ID
 * * userId: String - 用户ID
 * * templateType: String - 模版类型
 * * messages: String - 托管消息列表
 * * modelConfig: String - 模型配置
 * * variableDefs: String - 变量定义
 * * tools: String - tools
 * * toolCallConfig: String - tool调用配置
 * * baseVersion: String - 草稿关联版本
 * * isDraftEdited: Integer - 草稿内容是否基于BaseVersion有变更
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.PromptUserDraft结构体
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
 * - Go int32 -> Java Integer
 */
@Getter
@Setter
@ToString
@Table(name = "bt_prompt_user_draft")
public class PromptUserDraftEntity {

  /**
   * 主键ID
   * 迁移对应关系: Go语言PromptUserDraft.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间ID
   * 迁移对应关系: Go语言PromptUserDraft.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * Prompt ID
   * 迁移对应关系: Go语言PromptUserDraft.PromptID
   * - 功能: Prompt标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long promptId;

  /**
   * 用户ID
   * 迁移对应关系: Go语言PromptUserDraft.UserID
   * - 功能: 用户标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String userId;

  /**
   * 模版类型
   * 迁移对应关系: Go语言PromptUserDraft.TemplateType
   * - 功能: 模版类型
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(64), default Normal
   */
  private String templateType;

  /**
   * 托管消息列表
   * 迁移对应关系: Go语言PromptUserDraft.Messages
   * - 功能: 消息列表JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: longtext
   */
  private String messages;

  /**
   * 模型配置
   * 迁移对应关系: Go语言PromptUserDraft.ModelConfig
   * - 功能: 模型配置JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: text
   */
  private String modelConfig;

  /**
   * 变量定义
   * 迁移对应关系: Go语言PromptUserDraft.VariableDefs
   * - 功能: 变量定义JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: text
   */
  private String variableDefs;

  /**
   * tools
   * 迁移对应关系: Go语言PromptUserDraft.Tools
   * - 功能: 工具定义JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: longtext
   */
  private String tools;

  /**
   * tool调用配置
   * 迁移对应关系: Go语言PromptUserDraft.ToolCallConfig
   * - 功能: 工具调用配置JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: text
   */
  private String toolCallConfig;

  /**
   * 草稿关联版本
   * 迁移对应关系: Go语言PromptUserDraft.BaseVersion
   * - 功能: 草稿基于的版本
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String baseVersion;

  /**
   * 草稿内容是否基于BaseVersion有变更
   * 迁移对应关系: Go语言PromptUserDraft.IsDraftEdited
   * - 功能: 草稿编辑状态
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: tinyint(4), not null
   */
  private Integer isDraftEdited;

  /**
   * 创建时间
   * 迁移对应关系: Go语言PromptUserDraft.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言PromptUserDraft.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言PromptUserDraft.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: bigint(20), not null, default 0
   */
  private Long deletedAt;
}
