package com.iwhalecloud.bote.sandbox.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 删除文件结果
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@Schema(description = "沙箱内删除文件结果")
public class SandboxFileDeleteResult {
  /** 是否成功 */
  private boolean success;
  /** 错误信息 */
  private String errorMessage;

  /**
   * 构造成功结果
   */
  public static SandboxFileDeleteResult success() {
    return new SandboxFileDeleteResult(true, null);
  }

  /**
   * 构造失败结果
   */
  public static SandboxFileDeleteResult fail(String errorMessage) {
    return new SandboxFileDeleteResult(false, errorMessage);
  }
}
