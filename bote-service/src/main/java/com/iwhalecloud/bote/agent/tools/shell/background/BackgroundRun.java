package com.iwhalecloud.bote.agent.tools.shell.background;

import org.springframework.lang.Nullable;

/**
 * 后台执行句柄的通用接口（本地进程 / 沙箱异步任务等），用于统一 shellId 管理
 *
 * @author zhaoxu
 * @since 2026-03-10
 */
public interface BackgroundRun {

  /**
   * 获取自上次调用以来的新输出，可选正则过滤行
   *
   * @param filter 可选正则，仅保留匹配行
   * @return 格式化后的 STDOUT/STDERR 片段
   */
  String getNewOutput(@Nullable String filter);

  /**
   * 是否仍在运行
   */
  boolean isAlive();

  /**
   * 退出码；未结束时可为 -1 或未定义
   */
  int getExitCode();

  /**
   * 执行耗时（毫秒），未结束时可为 null
   */
  @Nullable
  Long getExecutionTimeMs();

  /**
   * 终止/取消本次后台执行
   */
  void destroy();
}
