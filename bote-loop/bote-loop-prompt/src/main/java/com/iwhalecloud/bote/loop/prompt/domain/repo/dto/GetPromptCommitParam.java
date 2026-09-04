package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取Prompt提交参数
 * 迁移对应关系: Go语言service.GetPromptCommitParam
 * - 功能: 获取Prompt提交信息时的参数封装
 * - 字段:
 * * commitVersion - 提交版本
 * <p>
 * Java实现说明:
 * - 对应Go的service.GetPromptCommitParam结构体
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
public class GetPromptCommitParam {

  @JsonProperty("commit_version")
  private String commitVersion;
}
