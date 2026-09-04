package com.iwhalecloud.bote.sandbox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 沙箱内命令执行结果
 */
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SandboxRunResult {
  /** 是否执行成功（无异常） */
  private boolean success;
  /** 错误信息（若异常） */
  private String errorMessage;
  /** 状态码 */
  private int exitCode;
  /** 标准输出行 */
  private String stdout;
  /** 标准错误行 */
  private String stderr;
}
