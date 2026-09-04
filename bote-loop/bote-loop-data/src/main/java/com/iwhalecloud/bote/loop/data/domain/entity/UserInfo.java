package com.iwhalecloud.bote.loop.data.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息实体
 * 迁移对应关系: Go语言entity.UserInfo
 * - 功能: 存储用户信息
 * - 字段定义:
 * * Name: *string - 用户名称
 * * EnName: *string - 英文名称
 * * AvatarURL: *string - 头像URL
 * * AvatarThumb: *string - 头像缩略图
 * * OpenID: *string - 开放ID
 * * UnionID: *string - 联合ID
 * * UserID: *string - 用户ID
 * * Email: *string - 邮箱
 * <p>
 * Java实现说明:
 * - 对应Go的entity.UserInfo结构体
 * - 使用Java类定义，包含用户信息字段
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
public class UserInfo {

  /**
   * 用户名称
   * 迁移对应关系: Go语言entity.UserInfo.Name (*string)
   * - 功能: 用户显示名称
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 用户界面显示
   */
  @JsonProperty("name")
  private String name;

  /**
   * 英文名称
   * 迁移对应关系: Go语言entity.UserInfo.EnName (*string)
   * - 功能: 用户英文名称
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 国际化显示
   */
  @JsonProperty("en_name")
  private String enName;

  /**
   * 头像URL
   * 迁移对应关系: Go语言entity.UserInfo.AvatarURL (*string)
   * - 功能: 用户头像链接
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 用户头像显示
   */
  @JsonProperty("avatar_url")
  private String avatarUrl;

  /**
   * 头像缩略图
   * 迁移对应关系: Go语言entity.UserInfo.AvatarThumb (*string)
   * - 功能: 用户头像缩略图链接
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 快速加载显示
   */
  @JsonProperty("avatar_thumb")
  private String avatarThumb;

  /**
   * 开放ID
   * 迁移对应关系: Go语言entity.UserInfo.OpenID (*string)
   * - 功能: 开放平台用户ID
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 第三方平台标识
   */
  @JsonProperty("open_id")
  private String openId;

  /**
   * 联合ID
   * 迁移对应关系: Go语言entity.UserInfo.UnionID (*string)
   * - 功能: 联合登录用户ID
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 多平台用户关联
   */
  @JsonProperty("union_id")
  private String unionId;

  /**
   * 用户ID
   * 迁移对应关系: Go语言entity.UserInfo.UserID (*string)
   * - 功能: 系统内部用户ID
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 系统内部标识
   */
  @JsonProperty("user_id")
  private String userId;

  /**
   * 邮箱
   * 迁移对应关系: Go语言entity.UserInfo.Email (*string)
   * - 功能: 用户邮箱地址
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 联系和通知
   */
  @JsonProperty("email")
  private String email;
}
