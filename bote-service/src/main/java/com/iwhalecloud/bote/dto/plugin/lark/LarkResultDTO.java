package com.iwhalecloud.bote.dto.plugin.lark;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书插件返回结果
 *
 * @author qian.sisheng
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString
public class LarkResultDTO<T> {
  /** 错误码， 0成功 */
  private String code;
  /** 数据 */
  private T data;
  /** 错误信息 */
  private String msg;
}
