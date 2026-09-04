package com.iwhalecloud.bote.dto.beyond;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应资源发布响应数据
 *
 * @author lizuyin
 * @since 2025-07-24
 */
@Getter
@Setter
@ToString
public class BeyondResourcePublishData {
  /** 百应资源主键ID（成功时返回） */
  private Long resourceId;
  /** 资源编码 */
  private String resourceCode;
  /** 博特资源主键ID（始终返回） */
  private Long resourceSourcePkId;
  /** 本条资源是否发布成功 */
  private Boolean success;
  /** 错误消息（失败时返回） */
  private String errorMessage;
  /** 错误码（失败时返回），1=重复发布，0=其他错误 */
  private Integer errorCode;

  /**
   * 判断响应是否成功
   * code为-1但errorCode为0的时候，表示资源以发布但暂未审核上架，此时资源发布状态依旧是成功
   *
   * @return true表示成功，false表示失败
   */
  public boolean isSuccess() {
    return (success != null && success) || (errorCode != null && errorCode == 1);
  }
}
