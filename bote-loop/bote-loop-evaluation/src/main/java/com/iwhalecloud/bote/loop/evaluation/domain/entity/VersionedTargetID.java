package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 版本化目标ID实体
 * 迁移对应关系: Go语言VersionedTargetID
 * - 功能: 版本化目标ID数据结构
 * - 字段: targetID, versionID
 * <p>
 * Java实现说明:
 * - 对应Go的VersionedTargetID结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionedTargetID {
  @JsonProperty("target_id")
  private Long targetId;

  @JsonProperty("version_id")
  private Long versionId;
}
