package com.iwhalecloud.bote.agent.tools.shell.background;

import com.iwhalecloud.bote.common.thread.ThreadPools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 本地进程的后台执行封装，实现 {@link BackgroundRun}。
 *
 * @author zhaoxu
 * @since 2026-03-10
 */
public final class LocalBackgroundProcess extends AbstractBackgroundRun {
  private final Process process;

  public LocalBackgroundProcess(Process process) {
    this.process = process;
    startReader(process.getInputStream(), true);
    startReader(process.getErrorStream(), false);
  }

  private void startReader(InputStream inputStream, boolean out) {
    ThreadPools.getCommon().submit(() -> {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (out) {
            appendStdout(line);
          }
          else {
            appendStderr(line);
          }
        }
      }
      catch (IOException e) {
        // process terminated or stream closed
      }
    });
  }

  @Override
  public boolean isAlive() {
    return process.isAlive();
  }

  @Override
  public int getExitCode() {
    return process.isAlive() ? -1 : process.exitValue();
  }

  @Override
  public void destroy() {
    process.destroy();
    try {
      if (!process.waitFor(5, TimeUnit.SECONDS)) {
        process.destroyForcibly();
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
    }
  }
}
