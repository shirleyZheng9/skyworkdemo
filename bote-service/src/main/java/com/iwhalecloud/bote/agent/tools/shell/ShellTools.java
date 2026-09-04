package com.iwhalecloud.bote.agent.tools.shell;

import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.tool.callback.FunctionToolCallback;
import com.iwhalecloud.bote.agent.tool.callback.ToolCallback;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.agent.tool.util.ToolCallResultUtil;
import com.iwhalecloud.bote.agent.tools.shell.background.BackgroundRun;
import com.iwhalecloud.bote.agent.tools.shell.background.LocalBackgroundProcess;
import com.iwhalecloud.bote.agent.tools.shell.background.SandboxBackgroundRun;
import com.iwhalecloud.bote.common.enums.SandboxMode;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunRequest;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Shell 相关工具。后台执行与维护（本地进程 / 沙箱异步）统一在本类通过 {@link BackgroundRun} 管理。
 *
 * @author bianjp
 * @since 2026-02-03
 */
@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public final class ShellTools {
  /** 工具名称: 执行命令 */
  public static final String TOOL_NAME_EXECUTE_SHELL_COMMAND = "execute_shell_command";
  /** 命令默认超时时间(ms) */
  private static final long COMMAND_DEFAULT_TIMEOUT = 120000;
  /** 命令最大超时时间(ms) */
  private static final long COMMAND_MAX_TIMEOUT = 600000;
  /** 命令最大输出字符数 */
  private static final int COMMAND_MAX_OUTPUT_CHARS = 30000;
  /** 命令输出截断标记 */
  private static final String COMMAND_OUTPUT_TRUNCATED_HINT = "\n... (output truncated)";
  /** 工具描述: 执行命令 */
  private static final String TOOL_DESCRIPTION_EXECUTE_SHELL_COMMAND = """
    Executes a given shell command with optional timeout.

    IMPORTANT: This tool is for terminal operations like git, npm, docker, etc. DO NOT use it for file operations (reading, writing, editing) - use the specialized tools for this instead.
    IMPORTANT: When the current operating system is Windows, DO NOT use Bash/Linux commands (e.g., ls, file, cat, pwd, grep). Use PowerShell commands instead.

    Usage notes:
    - Use uv to execute a Python script with dependencies: `uv run --with reportlab --with pypdf script.py`. NOTE: it's not necessary to manually create a virtual environment, uv will automatically create a temporary one with the required dependencies.
    - If the output exceeds 30000 characters, output will be truncated before being returned to you.
    - Use absolute path instead of `cd` unless absolutely necessary, for example: use `python3 /foo/bar/test.py` instead of `cd /foo/bar && python3 test.py`
    """;

  /** 所有后台执行（本地 + 沙箱）统一由此 map 维护 */
  private static final Map<String, BackgroundRun> backgroundRuns = new ConcurrentHashMap<>();

  private ShellTools() {
  }

  /**
   * 构造执行命令工具
   *
   * @param sandboxMode 沙箱模式
   * @param clientOperatingSystem 客户端操作系统，仅用于 client 沙箱模式
   */
  public static ToolCallback buildExecuteShellCommandTool(SandboxMode sandboxMode, @Nullable String clientOperatingSystem) {
    String operatingSystem;
    // 远程沙箱，固定是 Linux
    if (sandboxMode == SandboxMode.REMOTE) {
      operatingSystem = "Linux";
    }
    // 客户端，使用客户端操作系统
    else if (sandboxMode == SandboxMode.CLIENT) {
      operatingSystem = StringUtils.defaultIfEmpty(clientOperatingSystem, "Windows 10");
    }
    // 本地环境，自动探测
    else {
      operatingSystem = SystemUtils.OS_NAME + " " + SystemUtils.OS_VERSION;
    }
    String shellType = Strings.CI.contains(operatingSystem, "Windows") ? "PowerShell" : "Bash";
    String commandDescription = "The command to execute using %s syntax only. The current operating system is %s".formatted(shellType, operatingSystem);
    JsonSchemaNode parameters = JsonSchemaNode.newObject()
      .addProperty("command", commandDescription, JsonSchemaDataType.STRING, true)
      .addProperty("timeout", "Optional timeout in milliseconds (max 600000), default 120000", JsonSchemaDataType.INTEGER)
      .addProperty("description", "Clear, concise description of what this command does in 5-10 words, in active voice", JsonSchemaDataType.STRING);
    com.iwhalecloud.bote.llm.client.dto.Tool tool = new com.iwhalecloud.bote.llm.client.dto.Tool(
      TOOL_NAME_EXECUTE_SHELL_COMMAND,
      TOOL_DESCRIPTION_EXECUTE_SHELL_COMMAND,
      parameters);
    return new FunctionToolCallback<>(tool, ExecuteCommandRequest.class, ShellTools::executeShellCommandWrapper);
  }

  /**
   * 执行命令，封装执行结果
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private static Object executeShellCommandWrapper(ExecuteCommandRequest request, ToolContext toolContext) {
    String output = executeShellCommand(request, toolContext);
    // 从脚本输出中解析特殊事件
    ToolExecutionResult toolExecutionResult = ToolCallResultUtil.extractEvent(output, toolContext.tenantId());
    if (toolExecutionResult != null) {
      return toolExecutionResult;
    }
    return output;
  }

  /**
   * 执行命令
   */
  private static String executeShellCommand(ExecuteCommandRequest request, ToolContext toolContext) {
    Assert.hasText(request.command, "Error: command is required");
    if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("command", request.command);
      if (request.timeout != null) {
        params.put("timeout", request.timeout);
      }
      return toolContext.webSocketChatContext().invokeTool("execute_shell_command", params);
    }

    // 暂时屏蔽后台执行能力
    boolean runInBackground = false;

    long finalTimeout = Math.min(ObjectUtils.getIfNull(request.timeout, COMMAND_DEFAULT_TIMEOUT), COMMAND_MAX_TIMEOUT);
    String shellId = "shell_" + System.currentTimeMillis();
    if (toolContext.sandboxMode() == SandboxMode.REMOTE) {
      return runInSandbox(shellId, request.command, finalTimeout, runInBackground, toolContext);
    }
    return runLocally(shellId, request.command, finalTimeout, runInBackground, MapUtils.emptyIfNull(toolContext.envVariables()));
  }

  private static String runInSandbox(String shellId, String command, long timeout, @Nullable Boolean runInBackground,
                                     ToolContext toolContext) {
    if (Boolean.TRUE.equals(runInBackground)) {
      SandboxBackgroundRun run = new SandboxBackgroundRun();
      backgroundRuns.put(shellId, run);
      run.start(command, toolContext.sandboxClient());
      return String.format(
        "shellId: %s\n\nBackground shell started with ID: %s\nUse `shell_output` tool with shellId='%s' to retrieve output.",
        shellId, shellId, shellId);
    }
    SandboxRunRequest request = SandboxRunRequest.builder()
      .command(command)
      .timeout(timeout)
      .build();
    SandboxRunResult result = toolContext.sandboxClient().execute(request);
    String stdout = StringUtils.trimToEmpty(result.getStdout());
    String stderr = StringUtils.trimToEmpty(result.getStderr());
    String output = buildCommandResult(stdout, stderr, result.getExitCode());
    if (!result.isSuccess()) {
      throw new ToolExecutionException(output);
    }
    return output;
  }

  @SuppressFBWarnings("COMMAND_INJECTION")
  private static String runLocally(String shellId, String command, long finalTimeout, @Nullable Boolean runInBackground, Map<String, String> envVariables) {
    try {
      List<String> shellCommand = SystemUtils.IS_OS_WINDOWS
        ? List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-Command", command)
        : List.of("/bin/bash", "-c", command);
      ProcessBuilder processBuilder = new ProcessBuilder(shellCommand);
      processBuilder.environment().putAll(envVariables);
      processBuilder.redirectErrorStream(false);
      Process process = processBuilder.start();
      if (Boolean.TRUE.equals(runInBackground)) {
        backgroundRuns.put(shellId, new LocalBackgroundProcess(process));
        return String.format(
          "shellId: %s\n\nBackground shell started with ID: %s\nUse `shell_output` tool with shellId='%s' to retrieve output.",
          shellId, shellId, shellId);
      }
      StringBuilder stdout = new StringBuilder();
      StringBuilder stderr = new StringBuilder();
      readProcessOutput(process.getInputStream(), stdout);
      readProcessOutput(process.getErrorStream(), stderr);
      boolean completed = process.waitFor(finalTimeout, TimeUnit.MILLISECONDS);
      if (!completed) {
        process.destroy();
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
          process.destroyForcibly();
        }
        throw new ToolExecutionException(String.format("Error: Command timed out after %dms", finalTimeout));
      }
      int exitCode = process.exitValue();
      return buildCommandResult(stdout.toString().trim(), stderr.toString().trim(), exitCode);
    }
    catch (IOException e) {
      throw new ToolExecutionException("Error executing command: " + e.getMessage(), e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ToolExecutionException("Command execution interrupted: " + e.getMessage(), e);
    }
  }

  /**
   * 构造命令执行结果
   */
  private static String buildCommandResult(String stdout, String stderr, int exitCode) {
    StringBuilder result = new StringBuilder();
    if (exitCode != 0) {
      result.append("Exit code: ").append(exitCode);
    }
    if (!stdout.isEmpty()) {
      appendLineBreakIfMissing(result);
      boolean truncated = appendWithLimit(result, stdout);
      if (truncated) {
        return result.toString();
      }
    }
    if (!stderr.isEmpty()) {
      appendLineBreakIfMissing(result);
      result.append("STDERR:\n");
      appendWithLimit(result, stderr);
    }
    return result.toString();
  }

  /**
   * 末尾添加换行符
   */
  private static void appendLineBreakIfMissing(StringBuilder result) {
    if (!result.isEmpty() && result.charAt(result.length() - 1) != '\n') {
      result.append('\n');
    }
  }

  /**
   * 添加 stdout/stderr, 限制总长度
   */
  private static boolean appendWithLimit(StringBuilder target, String content) {
    int remaining = COMMAND_MAX_OUTPUT_CHARS - target.length();
    if (remaining > 0) {
      if (content.length() <= remaining) {
        target.append(content);
        return false;
      }
      target.append(content, 0, remaining);
    }
    target.append(COMMAND_OUTPUT_TRUNCATED_HINT);
    return true;
  }

  private static void readProcessOutput(InputStream inputStream, StringBuilder collector) {
    ThreadPools.getCommon().submit(() -> {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          collector.append(line).append("\n");
        }
      }
      catch (IOException e) {
        // ignore
      }
    });
  }

  // @Tool(name = "shell_output", description = "Retrieves output from a running or completed background shell. Returns only new output since the last check.")
  public static Object shellOutput(@ToolParam(description = "The ID of the background shell to retrieve output from") String shellId,
                                   @ToolParam(description = "Optional regular expression to filter the output lines. Only lines matching this regex will be included in the result. Any lines that do not match will no longer be available to read") @Nullable String filter,
                                   ToolContext toolContext) {
    // 使用 WebSocket 模式时在客户端执行
    if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("shellId", shellId);
      params.put("filter", filter);
      return toolContext.webSocketChatContext().invokeTool("shell_output", params);
    }

    BackgroundRun run = backgroundRuns.get(shellId);
    if (run == null) {
      throw new ToolExecutionException("Error: No background shell found with ID: " + shellId);
    }
    String newOutput = run.getNewOutput(filter);
    StringBuilder result = new StringBuilder();
    result.append("Shell ID: ").append(shellId).append("\n");
    result.append("Status: ").append(run.isAlive() ? "Running" : "Completed").append("\n");
    if (!run.isAlive() && run.getExitCode() >= 0) {
      result.append("Exit code: ").append(run.getExitCode()).append("\n");
    }
    if (run.getExecutionTimeMs() != null) {
      result.append("Execution time: ").append(run.getExecutionTimeMs()).append("ms\n");
    }
    if (!newOutput.isEmpty()) {
      result.append("\nNew output:\n").append(newOutput);
    }
    else {
      result.append("\nNo new output since last check.");
    }
    return result.toString();
  }

  // @Tool(name = "kill_shell", description = "Kills a running background shell by its ID")
  public static String killShell(@ToolParam(description = "The ID of the background shell to kill") String shellId,
                                 ToolContext toolContext) {
    // 使用 WebSocket 模式时在客户端执行
    if (toolContext.sandboxMode() == SandboxMode.CLIENT) {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("shellId", shellId);
      return toolContext.webSocketChatContext().invokeTool("kill_shell", params);
    }

    BackgroundRun run = backgroundRuns.get(shellId);
    if (run == null) {
      throw new ToolExecutionException("Error: No background shell found with ID: " + shellId);
    }
    if (!run.isAlive()) {
      backgroundRuns.remove(shellId);
      return "Shell " + shellId + " was already terminated. Removed from active shells.";
    }
    run.destroy();
    try {
      Thread.sleep(500);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    backgroundRuns.remove(shellId);
    return "Successfully killed shell: " + shellId;
  }


  /**
   * 执行命令请求
   *
   * @param command 命令
   * @param timeout 超时时间(ms)
   * @param description 描述
   */
  public record ExecuteCommandRequest(String command, @Nullable Long timeout, @Nullable String description) {
  }

}
