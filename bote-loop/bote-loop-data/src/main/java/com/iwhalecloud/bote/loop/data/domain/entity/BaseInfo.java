package com.iwhalecloud.bote.loop.data.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础信息实体
 * 迁移对应关系: Go语言entity.BaseInfo
 * - 功能: 存储基础信息
 * - 字段定义:
 * * CreatedBy: *UserInfo - 创建者信息
 * * UpdatedBy: *UserInfo - 更新者信息
 * * CreatedAt: *int64 - 创建时间
 * * UpdatedAt: *int64 - 更新时间
 * * DeletedAt: *int64 - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.BaseInfo结构体
 * - 使用Java类定义，包含基础信息字段
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
public class BaseInfo {

  /**
   * 创建者信息
   * 迁移对应关系: Go语言entity.BaseInfo.CreatedBy (*UserInfo)
   * - 功能: 创建者用户信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 记录创建者
   */
  @JsonProperty("created_by")
  private UserInfo createdBy;

  /**
   * 更新者信息
   * 迁移对应关系: Go语言entity.BaseInfo.UpdatedBy (*UserInfo)
   * - 功能: 更新者用户信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 记录更新者
   */
  @JsonProperty("updated_by")
  private UserInfo updatedBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言entity.BaseInfo.CreatedAt (*int64)
   * - 功能: 创建时间戳
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 记录创建时间
   */
  @JsonProperty("created_at")
  private Long createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言entity.BaseInfo.UpdatedAt (*int64)
   * - 功能: 更新时间戳
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 记录更新时间
   */
  @JsonProperty("updated_at")
  private Long updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言entity.BaseInfo.DeletedAt (*int64)
   * - 功能: 删除时间戳
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 软删除标记
   */
  @JsonProperty("deleted_at")
  private Long deletedAt;
}
