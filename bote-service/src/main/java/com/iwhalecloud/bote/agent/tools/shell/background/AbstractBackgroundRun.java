package com.iwhalecloud.bote.agent.tools.shell.background;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 后台执行通用抽象：维护 stdout/stderr 缓冲与增量读取、正则过滤，子类实现 isAlive/destroy 等。
 *
 * @author zhaoxu
 * @since 2026-03-10
 */
public abstract class AbstractBackgroundRun implements BackgroundRun {
  protected final StringBuilder stdout = new StringBuilder();
  protected final StringBuilder stderr = new StringBuilder();
  protected int lastStdoutPosition = 0;
  protected int lastStderrPosition = 0;

  @Override
  public final String getNewOutput(@Nullable String filter) {
    Pattern pattern = StringUtils.isEmpty(filter) ? null : Pattern.compile(filter);
    String newStdout;
    String newStderr;
    synchronized (stdout) {
      newStdout = stdout.substring(lastStdoutPosition);
      lastStdoutPosition = stdout.length();
    }
    synchronized (stderr) {
      newStderr = stderr.substring(lastStderrPosition);
      lastStderrPosition = stderr.length();
    }
    if (pattern != null) {
      newStdout = filterOutput(newStdout, pattern);
      newStderr = filterOutput(newStderr, pattern);
    }
    StringBuilder result = new StringBuilder();
    if (!newStdout.isEmpty()) {
      result.append("STDOUT:\n").append(newStdout);
    }
    if (!newStderr.isEmpty()) {
      if (!result.isEmpty()) {
        result.append("\n");
      }
      result.append("STDERR:\n").append(newStderr);
    }
    return result.toString();
  }

  @Override
  @Nullable
  public Long getExecutionTimeMs() {
    return null;
  }

  /** 子类或读者线程调用：追加一行 stdout */
  protected final void appendStdout(String line) {
    synchronized (stdout) {
      stdout.append(line).append("\n");
    }
  }

  /** 子类或读者线程调用：追加一行 stderr */
  protected final void appendStderr(String line) {
    synchronized (stderr) {
      stderr.append(line).append("\n");
    }
  }

  protected static String filterOutput(String output, Pattern pattern) {
    if (output.isEmpty()) {
      return "";
    }
    String[] lines = output.split("\n");
    StringBuilder filtered = new StringBuilder();
    for (String line : lines) {
      if (pattern.matcher(line).find()) {
        filtered.append(line).append("\n");
      }
    }
    return filtered.toString();
  }
}
