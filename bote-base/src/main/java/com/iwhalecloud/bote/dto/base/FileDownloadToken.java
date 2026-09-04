package com.iwhalecloud.bote.dto.base;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件下载Token信息
 * 用于kkfileview文件预览服务的临时访问token
 *
 * @author Aiqing
 * @since 2025-09-06
 */
@Getter
@Setter
@ToString
public class FileDownloadToken implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Token唯一标识
   */
  private String token;

  /**
   * 用户ID
   */
  private Long userId;

  /**
   * 文档ID
   */
  private String documentId;

  /**
   * 文件名称
   */
  private String fileName;

  /**
   * 文件ID
   */
  private Long fileId;

  /**
   * 文件大小
   */
  private Long fileSize;

  /**
   * Token创建时间
   */
  private LocalDateTime createTime;

  /**
   * Token过期时间
   */
  private LocalDateTime expireTime;

  /**
   * 文件的下载地址
   */
  private String downloadUrl;

  /**
   * 检查token是否过期
   *
   * @return 是否过期
   */
  public boolean isExpired() {
    return expireTime != null && LocalDateTime.now().isAfter(expireTime);
  }

  /**
   * 检查token是否有效（未过期且未使用）
   *
   * @return 是否有效
   */
  public boolean isValid() {
    return !isExpired();
  }

  /**
   * 获取剩余有效时间（秒）
   *
   * @return 剩余有效时间，如果已过期返回0
   */
  public long getRemainingSeconds() {
    if (expireTime == null) {
      return 0;
    }
    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(expireTime)) {
      return 0;
    }
    return java.time.Duration.between(now, expireTime).getSeconds();
  }
}
