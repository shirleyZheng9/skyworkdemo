package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集操作实体
 * 迁移对应关系: Go语言entity.DatasetOperation
 * - 功能: 存储数据集操作信息
 * - 字段定义:
 * * ID: string - 操作ID
 * * Type: DatasetOpType - 操作类型
 * * TS: time.Time - 时间戳
 * * TTL: time.Duration - 生存时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetOperation结构体
 * - 使用Java类定义，包含操作字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 包含字符串表示方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go time.Time -> Java LocalDateTime
 * - Go time.Duration -> Java Duration
 * - Go方法 -> Java方法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetOperation {

  @JsonProperty("id")
  private String id;

  @JsonProperty("type")
  private DatasetOpType type;

  @JsonProperty("ts")
  private LocalDateTime ts;

  @JsonProperty("ttl")
  private Duration ttl;

  /**
   * 获取字符串表示
   * 迁移对应关系: Go语言entity.DatasetOperation.String()
   * - 功能: 获取操作对象的字符串表示
   * - 返回: 字符串表示
   */
  @Override
  public String toString() {
    return String.format("{id=%s, type=%s, ts=%s, ttl=%s}", id, type, ts, ttl);
  }
}
