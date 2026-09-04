package com.iwhalecloud.bote.loop.client.prompt.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户信息详情DTO
 * 迁移对应关系: Go语言kitex_gen/coze/loop/prompt/domain/user.UserInfoDetail
 * - 功能: 用户详细信息的数据传输对象
 * - 字段映射:
 * * user_id -> userId (用户ID)
 * * name -> name (用户名称)
 * * nick_name -> nickName (用户昵称)
 * * avatar_url -> avatarUrl (头像URL)
 * * email -> email (邮箱)
 * * mobile -> mobile (手机号)
 * <p>
 * Java实现说明:
 * - 对应Go的UserInfoDetail结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解处理JSON序列化
 * - 所有字段都是可选的(对应Go的optional字段)
 * - 使用Builder模式构建对象
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go指针类型 -> Java对象引用
 * - Go optional字段 -> Java可空字段
 * - Go thrift标签 -> Java Jackson注解
 * - Go Getter/Setter -> Java Lombok自动生成
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDetailDTO {

  /**
   * 用户ID
   * 迁移对应关系: Go语言UserID *string
   * - 字段ID: 1
   * - 类型: 字符串
   * - 是否可选: 是
   * -- SETTER --
   * 设置用户ID
   * 迁移对应关系: Go语言SetUserID(val *string)
   */
  private String userId;

  /**
   * 用户名称
   * 迁移对应关系: Go语言Name *string
   * - 字段ID: 11
   * - 类型: 字符串
   * - 是否可选: 是
   * -- SETTER --
   * 设置用户名称
   * 迁移对应关系: Go语言SetName(val *string)
   */
  private String name;

  /**
   * 用户昵称
   * 迁移对应关系: Go语言NickName *string
   * - 字段ID: 12
   * - 类型: 字符串
   * - 是否可选: 是
   * -- SETTER --
   * 设置用户昵称
   * 迁移对应关系: Go语言SetNickName(val *string)
   */
  private String nickName;

  /**
   * 头像URL
   * 迁移对应关系: Go语言AvatarURL *string
   * - 字段ID: 13
   * - 类型: 字符串
   * - 是否可选: 是
   * -- SETTER --
   * 设置头像URL
   * 迁移对应关系: Go语言SetAvatarURL(val *string)
   */
  private String avatarUrl;

  /**
   * 邮箱
   * 迁移对应关系: Go语言Email *string
   * - 字段ID: 14
   * - 类型: 字符串
   * - 是否可选: 是
   * -- SETTER --
   * 设置邮箱
   * 迁移对应关系: Go语言SetEmail(val *string)
   */
  private String email;

  /**
   * 手机号
   * 迁移对应关系: Go语言Mobile *string
   * - 字段ID: 15
   * - 类型: 字符串
   * - 是否可选: 是
   * -- SETTER --
   * 设置手机号
   * 迁移对应关系: Go语言SetMobile(val *string)
   */
  private String mobile;
}
