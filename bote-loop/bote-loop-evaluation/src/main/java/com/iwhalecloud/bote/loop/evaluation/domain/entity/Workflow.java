package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Workflow实体
 * 迁移对应关系: Go语言CozeWorkflow
 * - 功能: Workflow数据结构
 * - 字段: id, version, endType, name, avatarUrl, description, baseInfo
 * <p>
 * Java实现说明:
 * - 对应Go的CozeWorkflow结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go int32 -> Java Integer
 * - Go *BaseInfo -> Java BaseInfo
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workflow {
  @JsonProperty("id")
  private String id;

  @JsonProperty("version")
  private String version;

  @JsonProperty("end_type")
  private Integer endType;

  @JsonProperty("name")
  private String name;

  @JsonProperty("avatar_url")
  private String avatarUrl;

  @JsonProperty("description")
  private String description;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;
}

