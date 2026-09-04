package com.iwhalecloud.bote.loop.data.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传令牌实体
 * 迁移对应关系: Go语言entity.UploadToken
 * - 功能: 存储上传令牌信息
 * - 字段定义:
 * * AccessKeyID: string - 访问密钥ID
 * * SecretAccessKey: string - 秘密访问密钥
 * * SessionToken: string - 会话令牌
 * * ExpiredTime: string - 过期时间
 * * CurrentTime: string - 当前时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.UploadToken结构体
 * - 使用Java类定义，包含上传令牌字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go字符串类型 -> Java字符串
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadToken {

  /**
   * 访问密钥ID
   * 迁移对应关系: Go语言entity.UploadToken.AccessKeyID (string)
   * - 功能: 访问密钥标识
   * - 类型: Go的字符串类型对应Java的字符串
   * - 用途: 用于身份验证
   */
  @JsonProperty("access_key_id")
  private String accessKeyId;

  /**
   * 秘密访问密钥
   * 迁移对应关系: Go语言entity.UploadToken.SecretAccessKey (string)
   * - 功能: 秘密访问密钥
   * - 类型: Go的字符串类型对应Java的字符串
   * - 用途: 用于身份验证
   */
  @JsonProperty("secret_access_key")
  private String secretAccessKey;

  /**
   * 会话令牌
   * 迁移对应关系: Go语言entity.UploadToken.SessionToken (string)
   * - 功能: 会话令牌
   * - 类型: Go的字符串类型对应Java的字符串
   * - 用途: 用于临时访问
   */
  @JsonProperty("session_token")
  private String sessionToken;

  /**
   * 过期时间
   * 迁移对应关系: Go语言entity.UploadToken.ExpiredTime (string)
   * - 功能: 令牌过期时间
   * - 类型: Go的字符串类型对应Java的字符串
   * - 用途: 控制令牌有效期
   */
  @JsonProperty("expired_time")
  private String expiredTime;

  /**
   * 当前时间
   * 迁移对应关系: Go语言entity.UploadToken.CurrentTime (string)
   * - 功能: 当前时间
   * - 类型: Go的字符串类型对应Java的字符串
   * - 用途: 时间戳记录
   */
  @JsonProperty("current_time")
  private String currentTime;
}
