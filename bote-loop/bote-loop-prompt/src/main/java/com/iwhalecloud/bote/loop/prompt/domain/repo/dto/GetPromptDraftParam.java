package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取Prompt草稿参数
 * 迁移对应关系: Go语言service.GetPromptDraftParam
 * - 功能: 获取Prompt草稿信息时的参数封装
 * - 字段:
 * * userId - 用户ID
 * <p>
 * Java实现说明:
 * - 对应Go的service.GetPromptDraftParam结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetPromptDraftParam {

  @JsonProperty("user_id")
  private String userId;
}
