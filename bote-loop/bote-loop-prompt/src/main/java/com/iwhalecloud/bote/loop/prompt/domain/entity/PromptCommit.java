package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt提交信息
 * 迁移对应关系: Go语言entity.PromptCommit
 * - 功能: 存储Prompt的提交信息
 * - 字段定义:
 * * PromptDetail: *PromptDetail - 详细信息
 * * CommitInfo: *CommitInfo - 提交元数据
 * <p>
 * Java实现说明:
 * - 对应Go的entity.PromptCommit结构体
 * - 使用Java类定义，包含提交相关字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptCommit {

  /**
   * 详细信息
   * 迁移对应关系: Go语言entity.PromptCommit.PromptDetail (*PromptDetail)
   * - 功能: Prompt的详细信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储提交的完整内容
   */
  @JsonProperty("prompt_detail")
  private PromptDetail promptDetail;

  /**
   * 提交信息
   * 迁移对应关系: Go语言entity.PromptCommit.CommitInfo (*CommitInfo)
   * - 功能: 提交的元数据信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储提交状态和版本信息
   */
  @JsonProperty("commit_info")
  private CommitInfo commitInfo;
}
