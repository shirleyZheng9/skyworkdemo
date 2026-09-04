package com.iwhalecloud.bote.sandbox.api;

/**
 * 沙箱命令执行过程中的输出回调，用于流式收集 stdout/stderr（不依赖返回结果）。
 *
 * @author zhaoxu
 * @since 2026-03-10
 */
public interface SandboxOutputHandlers {

  /**
   * 收到一行 stdout
   */
  void onStdout(String line);

  /**
   * 收到一行 stderr
   */
  void onStderr(String line);

  /**
   * 命令执行结束
   *
   * @param executionTimeMs 执行耗时（毫秒）
   * @param success         是否成功
   */
  void onComplete(long executionTimeMs, boolean success);
}
