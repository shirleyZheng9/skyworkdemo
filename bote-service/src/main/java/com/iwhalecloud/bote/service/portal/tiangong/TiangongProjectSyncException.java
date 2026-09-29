package com.iwhalecloud.bote.service.portal.tiangong;

import lombok.Getter;

/**
 * 天工项目成员同步业务异常
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * 使用稳定错误码让 MQ 消费端区分可重试的项目尚未落库与不可重试的数据错误。
 */
@Getter
public class TiangongProjectSyncException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  /** 供 MQ 消费端分类处理的稳定业务错误码。 */
  private final String code;

  /** 构造带有稳定错误码和可读错误说明的同步异常。 */
  public TiangongProjectSyncException(String code, String message) {
    super(message);
    this.code = code;
  }
}
