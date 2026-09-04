package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础信息结构体
 * 迁移对应关系: Go语言BaseInfo
 * - 功能: 基础信息数据结构
 * - 字段: createdBy, updatedBy, createdAt, updatedAt, deletedAt
 * <p>
 * Java实现说明:
 * - 对应Go的BaseInfo结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现GetCreatedBy、SetCreatedBy等方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *UserInfo -> Java UserInfo
 * - Go *int64 -> Java Long
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseInfo {
  @JsonProperty("created_by")
  private UserInfo createdBy;

  @JsonProperty("updated_by")
  private UserInfo updatedBy;

  @JsonProperty("created_at")
  private Long createdAt;

  @JsonProperty("updated_at")
  private Long updatedAt;

  @JsonProperty("deleted_at")
  private Long deletedAt;

  /**
   * 获取创建者
   * 迁移对应关系: Go语言BaseInfo.GetCreatedBy()
   */
  public UserInfo getCreatedBy() {
    return this.createdBy;
  }

  /**
   * 设置创建者
   * 迁移对应关系: Go语言BaseInfo.SetCreatedBy()
   */
  public void setCreatedBy(UserInfo createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * 获取更新者
   * 迁移对应关系: Go语言BaseInfo.GetUpdatedBy()
   */
  public UserInfo getUpdatedBy() {
    return this.updatedBy;
  }

  /**
   * 设置更新者
   * 迁移对应关系: Go语言BaseInfo.SetUpdatedBy()
   */
  public void setUpdatedBy(UserInfo updatedBy) {
    this.updatedBy = updatedBy;
  }

  /**
   * 设置更新时间
   * 迁移对应关系: Go语言BaseInfo.SetUpdatedAt()
   */
  public void setUpdatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
  }
}
