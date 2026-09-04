package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息结构体
 * 迁移对应关系: Go语言UserInfo
 * - 功能: 用户信息数据结构
 * - 字段: name, enName, avatarUrl, avatarThumb, openId, unionId, userId, email
 * <p>
 * Java实现说明:
 * - 对应Go的UserInfo结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
  private String name;

  private String enName;

  private String avatarUrl;

  private String avatarThumb;

  private String openId;

  private String unionId;

  private String userId;

  private String email;
}
