package com.iwhalecloud.bote.sandbox.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 读取文件结果
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class SandboxFileReadResult {
  /** 是否成功 */
  private boolean success;
  /** 文件内容 */
  private String content;
  /** 错误信息 */
  private String errorMessage;

  /**
   * 构造成功结果
   */
  public static SandboxFileReadResult success(String content) {
    return new SandboxFileReadResult(true, content, null);
  }

  /**
   * 构造失败结果
   */
  public static SandboxFileReadResult fail(String errorMessage) {
    return new SandboxFileReadResult(false, null, errorMessage);
  }
}
