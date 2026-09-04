package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应通用响应对象
 *
 * @param <T> 响应数据类型
 * @author lizuyin
 * @since 2025-07-29
 */
@Getter
@Setter
@ToString
public class BeyondResponse<T> {
  /** 结果代码，0表示成功，-1表示失败 */
  private Integer code;
  /** 响应结果 */
  private Integer errorCode;
  /** 结果消息 */
  private String msg;
  /** 响应数据 */
  private T data;
  /** 操作结果，true表示成功，false表示失败 */
  private Boolean success;

  /**
   * 判断响应是否成功
   * code为-1但errorCode为0的时候，表示资源以发布但暂未审核上架，此时资源发布状态依旧是成功
   *
   * @return true表示成功，false表示失败
   */
  public boolean isSuccess() {
    return code != null && code == 0;
  }
}
