package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt核心实体
 * 迁移对应关系: Go语言entity.Prompt
 * - 功能: Prompt的核心数据结构
 * - 字段定义:
 * * ID: int64 - Prompt ID
 * * SpaceID: int64 - 空间ID
 * * PromptKey: string - Prompt键
 * * PromptBasic: *PromptBasic - 基础信息
 * * PromptDraft: *PromptDraft - 草稿信息
 * * PromptCommit: *PromptCommit - 提交信息
 * <p>
 * Java实现说明:
 * - 对应Go的entity.Prompt结构体
 * - 使用Java类定义，包含所有核心字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现Clone、CloneDetail、GetVersion等方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prompt {

  /**
   * Prompt ID
   * 迁移对应关系: Go语言entity.Prompt.ID (int64)
   * - 功能: Prompt的唯一标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 数据库主键和业务标识
   */
  @JsonProperty("id")
  private Long id;

  /**
   * 空间ID
   * 迁移对应关系: Go语言entity.Prompt.SpaceID (int64)
   * - 功能: 所属空间的标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 空间隔离和权限控制
   */
  @JsonProperty("space_id")
  private Long spaceId;

  /**
   * Prompt键
   * 迁移对应关系: Go语言entity.Prompt.PromptKey (string)
   * - 功能: Prompt的业务键
   * - 类型: Go的string对应Java的String
   * - 用途: 业务标识和版本管理
   */
  @JsonProperty("prompt_key")
  private String promptKey;

  /**
   * 基础信息
   * 迁移对应关系: Go语言entity.Prompt.PromptBasic (*PromptBasic)
   * - 功能: Prompt的基础信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储Prompt的基本元数据
   */
  @JsonProperty("prompt_basic")
  private PromptBasic promptBasic;

  /**
   * 草稿信息
   * 迁移对应关系: Go语言entity.Prompt.PromptDraft (*PromptDraft)
   * - 功能: Prompt的草稿信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储草稿状态和内容
   */
  @JsonProperty("prompt_draft")
  private PromptDraft promptDraft;

  /**
   * 提交信息
   * 迁移对应关系: Go语言entity.Prompt.PromptCommit (*PromptCommit)
   * - 功能: Prompt的提交信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储提交状态和内容
   */
  @JsonProperty("prompt_commit")
  private PromptCommit promptCommit;
  /** 目录ID */
  private Long catalogItemId;
  /**
   * 获取版本
   * 迁移对应关系: Go语言entity.Prompt.GetVersion() string
   * - 功能: 获取Prompt的版本号
   * - 返回: 版本字符串
   */
  public String getVersion() {
    if (this.promptCommit != null && this.promptCommit.getCommitInfo() != null) {
      return this.promptCommit.getCommitInfo().getVersion();
    }
    return "";
  }

  /**
   * 获取Prompt详情
   * 迁移对应关系: Go语言entity.Prompt.GetPromptDetail() *PromptDetail
   * - 功能: 获取Prompt的详细信息
   * - 返回: PromptDetail对象
   */
  public PromptDetail getPromptDetail() {
    if (this.promptDraft != null && this.promptDraft.getPromptDetail() != null) {
      return this.promptDraft.getPromptDetail();
    }
    if (this.promptCommit != null && this.promptCommit.getPromptDetail() != null) {
      return this.promptCommit.getPromptDetail();
    }
    return null;
  }

  /**
   * 格式化消息
   * 迁移对应关系: Go语言entity.Prompt.FormatMessages
   * - 功能: 根据Prompt模板格式化消息
   * - 参数: 消息列表、变量值列表
   * - 返回: 格式化后的消息列表
   */
  public java.util.List<Message> formatMessages(java.util.List<Message> messages, java.util.List<VariableVal> variableVals) {

    PromptDetail promptDetail = this.getPromptDetail();
    if (promptDetail == null) {
      return null;
    }

    PromptTemplate promptTemplate = promptDetail.getPromptTemplate();
    if (promptTemplate == null) {
      return null;
    }

    return promptTemplate.formatMessages(messages, variableVals);
  }

  /**
   * 获取模板消息
   * 迁移对应关系: Go语言entity.Prompt.GetTemplateMessages
   * - 功能: 获取模板消息
   * - 参数: 消息列表
   * - 返回: 模板消息列表
   */
  public java.util.List<Message> getTemplateMessages(java.util.List<Message> messages) {

    PromptDetail promptDetail = this.getPromptDetail();
    if (promptDetail == null) {
      return null;
    }

    PromptTemplate promptTemplate = promptDetail.getPromptTemplate();
    if (promptTemplate == null) {
      return null;
    }

    return promptTemplate.getTemplateMessages(messages);
  }
}
