package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运行错误实体
 * 对应Go: entity.RunError
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunError {

  /**
   * 错误代码
   * 对应Go: Code int64
   */
  private Long code;

  /**
   * 错误消息
   * 对应Go: Message *string
   */
  private String message;

  /**
   * 错误详情
   * 对应Go: Detail *string
   */
  private String detail;
}
