package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取基础信息参数
 * 迁移对应关系: Go语言service.GetBasicParam
 * - 功能: 获取Prompt基础信息时的参数封装
 * - 字段:
 * * promptId - Prompt ID
 * * spaceId - 空间ID
 * * promptKey - Prompt键
 * <p>
 * Java实现说明:
 * - 对应Go的service.GetBasicParam结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
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
public class GetBasicParam {

  @JsonProperty("prompt_id")
  private Long promptId;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("prompt_key")
  private String promptKey;
}
