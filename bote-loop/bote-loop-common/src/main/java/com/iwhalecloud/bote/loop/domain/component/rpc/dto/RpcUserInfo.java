package com.iwhalecloud.bote.loop.domain.component.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息实体类
 * 迁移对应关系: Go语言modules/prompt/domain/component/rpc.UserInfo
 * - 功能: 用户信息的数据传输对象
 * - 字段映射:
 * * user_id -> userId (用户ID)
 * * user_name -> userName (用户名称)
 * * nick_name -> nickName (用户昵称)
 * * avatar_url -> avatarUrl (头像URL)
 * * email -> email (邮箱)
 * * mobile -> mobile (手机号)
 * <p>
 * Java实现说明:
 * - 对应Go的UserInfo结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解处理JSON序列化
 * - 所有字段都是必需的(对应Go的非指针字段)
 * - 使用Builder模式构建对象
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string字段 -> Java String字段
 * - Go json标签 -> Java Jackson注解
 * - Go Getter/Setter -> Java Lombok自动生成
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcUserInfo {

  /**
   * 用户ID
   * 迁移对应关系: Go语言UserID string
   */
  @JsonProperty("user_id")
  private String userId;

  /**
   * 用户名称
   * 迁移对应关系: Go语言UserName string
   */
  @JsonProperty("user_name")
  private String userName;

  /**
   * 用户昵称
   * 迁移对应关系: Go语言NickName string
   */
  @JsonProperty("nick_name")
  private String nickName;

  /**
   * 头像URL
   * 迁移对应关系: Go语言AvatarURL string
   */
  @JsonProperty("avatar_url")
  private String avatarUrl;

  /**
   * 邮箱
   * 迁移对应关系: Go语言Email string
   */
  @JsonProperty("email")
  private String email;

  /**
   * 手机号
   * 迁移对应关系: Go语言Mobile string
   */
  @JsonProperty("mobile")
  private String mobile;
}
