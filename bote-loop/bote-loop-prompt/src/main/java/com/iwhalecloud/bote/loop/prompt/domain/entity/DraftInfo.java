package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 草稿信息实体
 * 迁移对应关系: Go语言entity.DraftInfo
 * - 功能: 存储草稿的元数据信息
 * - 字段定义:
 * * UserID: string - 用户ID
 * * BaseVersion: string - 基础版本
 * * IsModified: bool - 是否已修改
 * * CreatedAt: time.Time - 创建时间
 * * UpdatedAt: time.Time - 更新时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DraftInfo结构体
 * - 使用Java类定义，包含getter/setter方法
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 时间类型使用LocalDateTime替代Go的time.Time
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go time.Time -> Java LocalDateTime
 * - Go bool -> Java Boolean
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftInfo {

  /**
   * 用户ID
   * 迁移对应关系: Go语言entity.DraftInfo.UserID (string)
   * - 功能: 草稿创建者的用户标识
   * - 类型: Go的string对应Java的String
   * - 用途: 权限控制和审计
   */
  @JsonProperty("user_id")
  private String userId;

  /**
   * 基础版本
   * 迁移对应关系: Go语言entity.DraftInfo.BaseVersion (string)
   * - 功能: 草稿基于的版本号
   * - 类型: Go的string对应Java的String
   * - 用途: 版本管理和追踪
   */
  @JsonProperty("base_version")
  private String baseVersion;

  /**
   * 是否已修改
   * 迁移对应关系: Go语言entity.DraftInfo.IsModified (bool)
   * - 功能: 标识草稿是否已被修改
   * - 类型: Go的bool对应Java的Boolean
   * - 用途: 状态管理和UI显示
   */
  @JsonProperty("is_modified")
  private Boolean isModified;

  /**
   * 创建时间
   * 迁移对应关系: Go语言entity.DraftInfo.CreatedAt (time.Time)
   * - 功能: 草稿的创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 用途: 审计和时间追踪
   */
  @JsonProperty("created_at")
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言entity.DraftInfo.UpdatedAt (time.Time)
   * - 功能: 草稿的最后更新时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 用途: 审计和时间追踪
   */
  @JsonProperty("updated_at")
  private Date updatedAt;

}
