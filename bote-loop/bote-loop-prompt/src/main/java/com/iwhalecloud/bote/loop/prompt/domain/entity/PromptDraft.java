package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt草稿信息
 * 迁移对应关系: Go语言entity.PromptDraft
 * - 功能: 存储Prompt的草稿信息
 * - 字段定义:
 * * PromptDetail: *PromptDetail - 详细信息
 * * DraftInfo: *DraftInfo - 草稿元数据
 * <p>
 * Java实现说明:
 * - 对应Go的entity.PromptDraft结构体
 * - 使用Java类定义，包含草稿相关字段
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
public class PromptDraft {

  /**
   * 详细信息
   * 迁移对应关系: Go语言entity.PromptDraft.PromptDetail (*PromptDetail)
   * - 功能: Prompt的详细信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储草稿的完整内容
   */
  @JsonProperty("prompt_detail")
  private PromptDetail promptDetail;

  /**
   * 草稿信息
   * 迁移对应关系: Go语言entity.PromptDraft.DraftInfo (*DraftInfo)
   * - 功能: 草稿的元数据信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储草稿状态和创建信息
   */
  @JsonProperty("draft_info")
  private DraftInfo draftInfo;

}
