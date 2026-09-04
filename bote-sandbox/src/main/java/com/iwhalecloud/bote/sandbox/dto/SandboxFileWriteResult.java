package com.iwhalecloud.bote.sandbox.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 写文件结果
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class SandboxFileWriteResult {
  /** 是否成功 */
  private boolean success;
  /** 错误信息 */
  private String errorMessage;

  /**
   * 构造成功结果
   */
  public static SandboxFileWriteResult success() {
    return new SandboxFileWriteResult(true, null);
  }

  /**
   * 构造失败结果
   */
  public static SandboxFileWriteResult fail(String errorMessage) {
    return new SandboxFileWriteResult(false, errorMessage);
  }
}
