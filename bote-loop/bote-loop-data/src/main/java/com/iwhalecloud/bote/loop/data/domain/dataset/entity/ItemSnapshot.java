package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目快照实体
 * 迁移对应关系: Go语言entity.ItemSnapshot
 * - 功能: 存储项目快照信息
 * - 字段定义:
 * * ID: int64 - 快照ID
 * * VersionID: int64 - 版本ID
 * * Snapshot: *Item - 快照项目
 * * CreatedAt: time.Time - 创建时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ItemSnapshot结构体
 * - 使用Java类定义，包含项目快照字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 包含ID访问方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go time.Time -> Java LocalDateTime
 * - Go方法 -> Java方法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSnapshot {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("version_id")
  private Long versionId;

  @JsonProperty("snapshot")
  private Item snapshot;

  @JsonProperty("created_at")
  private Date createdAt;
}
