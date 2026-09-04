package com.iwhalecloud.bote.agent.tools.shell.background;

import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.sandbox.api.SandboxClient;
import com.iwhalecloud.bote.sandbox.api.SandboxOutputHandlers;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunRequest;
import java.util.concurrent.Future;
import org.springframework.lang.Nullable;

/**
 * 沙箱后台执行封装
 *
 * @author zhaoxu
 * @since 2026-03-10
 */
public class SandboxBackgroundRun extends AbstractBackgroundRun {
  private volatile Future<?> future;
  private volatile boolean completed;
  private volatile long executionTimeMs;
  private volatile int exitCode = -1;

  /**
   * 启动后台任务
   *
   * @param command 命令
   * @param sandbox 沙箱服务（调用 executeWithHandlers，内部用 RunCommandRequest 跑命令）
   */
  public void start(String command, SandboxClient sandbox) {
    SandboxRunRequest request = SandboxRunRequest.builder().command(command).build();
    SandboxOutputHandlers handlers = new SandboxOutputHandlers() {
      @Override
      public void onStdout(String line) {
        appendStdout(line);
      }

      @Override
      public void onStderr(String line) {
        appendStderr(line);
      }

      @Override
      public void onComplete(long executionTimeMs, boolean success) {
        SandboxBackgroundRun.this.executionTimeMs = executionTimeMs;
        exitCode = success ? 0 : 1;
        completed = true;
      }
    };
    this.future = ThreadPools.getCommon().submit(() -> {
      try {
        sandbox.executeWithHandlers(request, handlers);
      }
      catch (Exception e) {
        appendStderr(e.getClass().getSimpleName() + ": " + e.getMessage());
        if (!completed) {
          exitCode = 1;
          completed = true;
        }
      }
    });
  }

  @Override
  public boolean isAlive() {
    return !completed;
  }

  @Override
  public int getExitCode() {
    return completed ? exitCode : -1;
  }

  @Override
  @Nullable
  public Long getExecutionTimeMs() {
    return completed ? executionTimeMs : null;
  }

  @Override
  public void destroy() {
    if (future != null) {
      future.cancel(true);
      future = null;
    }
    completed = true;
  }
}
