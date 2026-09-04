package com.iwhalecloud.bote.sandbox.runtime;

import com.alibaba.opensandbox.sandbox.domain.models.execd.executions.Execution;
import com.alibaba.opensandbox.sandbox.domain.models.execd.executions.ExecutionError;
import com.alibaba.opensandbox.sandbox.domain.models.execd.executions.ExecutionHandlers;
import com.alibaba.opensandbox.sandbox.domain.models.execd.executions.OutputMessage;
import com.alibaba.opensandbox.sandbox.domain.models.execd.executions.RunCommandRequest;
import com.alibaba.opensandbox.sandbox.domain.models.execd.filesystem.EntryInfo;
import com.alibaba.opensandbox.sandbox.domain.models.execd.filesystem.SearchEntry;
import com.alibaba.opensandbox.sandbox.domain.models.execd.filesystem.WriteEntry;
import com.alibaba.opensandbox.sandbox.domain.services.Commands;
import com.alibaba.opensandbox.sandbox.domain.services.Filesystem;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.sandbox.api.SandboxOutputHandlers;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileDeleteResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileReadResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileSearchResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileWriteResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunRequest;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunResult;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import kotlin.time.Duration;
import kotlin.time.DurationKt;
import kotlin.time.DurationUnit;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 基于 OpenSandbox 的沙箱运行时抽象类
 *
 * @author bianjp
 * @since 2026-05-08
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractOpenSandboxRuntime implements SandboxRuntime {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** 沙箱 ID */
  protected final String sandboxId;
  private final Commands commandService;
  private final Filesystem filesystemService;

  public AbstractOpenSandboxRuntime(String sandboxId, Commands commandService, Filesystem filesystemService) {
    this.sandboxId = sandboxId;
    this.commandService = commandService;
    this.filesystemService = filesystemService;
  }

  @Override
  public String getSandboxId() {
    return sandboxId;
  }

  @Override
  public SandboxRunResult execute(SandboxRunRequest request) {
    RunCommandRequest.Builder builder = RunCommandRequest.builder()
      .command(request.getCommand())
      .workingDirectory(request.getWorkDir())
      .envs(MapUtils.emptyIfNull(request.getEnv()));
    setCommandTimeout(builder, request.getTimeout());
    RunCommandRequest runCommandRequest = builder.build();
    Execution execution = commandService.run(runCommandRequest);
    List<String> stdout = execution.getLogs().getStdout().stream().map(OutputMessage::getText).toList();
    List<String> stderr = execution.getLogs().getStderr().stream().map(OutputMessage::getText).toList();

    ExecutionError error = execution.getError();
    int exitCode = ObjectUtils.getIfNull(execution.getExitCode(), 0);
    boolean success = error == null && exitCode == 0;
    String errorMessage = null;
    if (error != null) {
      if (CollectionUtils.isNotEmpty(error.getTraceback())) {
        errorMessage = String.join("\n", error.getTraceback());
      }
      else {
        errorMessage = error.getName() + "(" + error.getValue() + ")";
      }
      if (!stderr.isEmpty()) {
        errorMessage += "\n" + String.join("\n", stderr);
      }
      if (exitCode == 0) {
        if ("CommandExecError".equals(error.getName()) && StringUtils.isNumeric(error.getValue())) {
          exitCode = Integer.parseInt(error.getValue());
        }
        else {
          exitCode = 1;
        }
      }
    }

    return SandboxRunResult.builder()
      .stdout(stdout.isEmpty() ? null : String.join("\n", stdout))
      .stderr(stderr.isEmpty() ? null : String.join("\n", stderr))
      .success(success)
      .errorMessage(errorMessage)
      .exitCode(exitCode)
      .build();
  }

  @Override
  public void executeWithHandlers(SandboxRunRequest request, SandboxOutputHandlers handlers, long startMs) {
    ExecutionHandlers executionHandlers = ExecutionHandlers.builder()
      .onStdout(msg -> handlers.onStdout(msg.getText()))
      .onStderr(msg -> handlers.onStderr(msg.getText()))
      .onExecutionComplete(complete -> handlers.onComplete(complete.getExecutionTimeInMillis(), true))
      .build();
    RunCommandRequest.Builder builder = RunCommandRequest.builder()
      .command(request.getCommand())
      .workingDirectory(request.getWorkDir())
      .envs(MapUtils.emptyIfNull(request.getEnv()))
      .handlers(executionHandlers)
      .background(false);
    setCommandTimeout(builder, request.getTimeout());
    RunCommandRequest runCommandRequest = builder.build();
    try {
      commandService.run(runCommandRequest);
    }
    catch (Exception e) {
      logger.error("Execute command with handlers in sandbox failed: sandboxId={}, command={}", sandboxId, request.getCommand(), e);
      handlers.onStderr(e.getClass().getSimpleName() + ": " + e.getMessage());
      handlers.onComplete(System.currentTimeMillis() - startMs, false);
    }
  }

  /**
   * 设置命令超时时间
   */
  private void setCommandTimeout(RunCommandRequest.Builder builder, @Nullable Long timeout) {
    if (timeout == null || timeout <= 0) {
      return;
    }
    // 由于 Kotlin name mangling 问题暂时无法直接调用 builder.timeout 方法, 通过反射绕过
    // https://github.com/alibaba/OpenSandbox/issues/849
    try {
      long duration = DurationKt.toDuration(timeout, DurationUnit.MILLISECONDS);
      Object boxedDuration = MethodUtils.invokeStaticMethod(Duration.class, "box-impl", duration);
      FieldUtils.writeField(builder, "timeout", boxedDuration, true);
    }
    catch (Exception e) {
      logger.warn("Failed to set command timeout", e);
    }
  }

  @Override
  public SandboxFileReadResult readFile(String path, @Nullable String encoding) {
    try {
      String finalEncoding = StringUtils.defaultIfEmpty(encoding, StandardCharsets.UTF_8.name());
      String content = filesystemService.readFile(path, finalEncoding, null);
      return SandboxFileReadResult.success(content);
    }
    catch (Exception e) {
      String msg = ExpUtil.getMsg(e);
      // 文件不存在
      if (msg.contains("FILE_NOT_FOUND")) {
        logger.warn("Read file in sandbox failed, file not found: sandboxId={}, path={}, error={}", sandboxId, path, msg);
        return SandboxFileReadResult.fail("file not found");
      }
      logger.error("Read file in sandbox failed: sandboxId={}, path={}", sandboxId, path, e);
      return SandboxFileReadResult.fail(msg);
    }
  }

  @Override
  public InputStream readFileStream(String path) {
    try {
      return filesystemService.readStream(path, null);
    }
    catch (Exception e) {
      String msg = ExpUtil.getMsg(e);
      // 文件不存在
      if (msg.contains("FILE_NOT_FOUND")) {
        logger.warn("Read file stream in sandbox failed, file not found: sandboxId={}, path={}, error={}", sandboxId, path, msg);
        throw new BssException("读取沙箱文件失败: 文件不存在", e);
      }
      logger.error("Read file stream in sandbox failed: sandboxId={}, path={}", sandboxId, path, e);
      throw new BssException("读取沙箱文件失败: " + msg, e);
    }
  }

  @Override
  public SandboxFileWriteResult writeFile(String path, Object data, @Nullable Integer mode) {
    try {
      WriteEntry entry = WriteEntry.builder()
        .path(path)
        .data(data)
        .mode(ObjectUtils.getIfNull(mode, 644))
        .build();
      filesystemService.write(List.of(entry));
      return SandboxFileWriteResult.success();
    }
    catch (Exception e) {
      logger.error("Write file in sandbox failed: sandboxId={}, path={}", sandboxId, path, e);
      return SandboxFileWriteResult.fail(e.getMessage());
    }
  }

  @Override
  public SandboxFileSearchResult searchFiles(String path, @Nullable String pattern) {
    try {
      SearchEntry searchEntry = SearchEntry.builder()
        .path(path)
        .pattern(StringUtils.defaultIfEmpty(pattern, "*"))
        .build();
      List<EntryInfo> list = filesystemService.search(searchEntry);
      List<String> entries = list.stream().map(EntryInfo::getPath).collect(Collectors.toList());
      return SandboxFileSearchResult.success(entries);
    }
    catch (Exception e) {
      // 指定的搜索目录不存在时请求会报 404，当作成功
      if (Strings.CS.containsAny(e.getMessage(), "404 NOT_FOUND", "404 Not Found")) {
        return SandboxFileSearchResult.success(List.of());
      }
      logger.error("Search files in sandbox failed: sandboxId={}, path={}", sandboxId, path, e);
      return SandboxFileSearchResult.fail(e.getMessage());
    }
  }

  @Override
  public SandboxFileDeleteResult deleteFiles(List<String> paths) {
    try {
      filesystemService.deleteFiles(paths);
      return SandboxFileDeleteResult.success();
    }
    catch (Exception e) {
      logger.error("Delete files in sandbox failed: sandboxId={}, paths={}", sandboxId, paths, e);
      return SandboxFileDeleteResult.fail(ExpUtil.getMsg(e));
    }
  }
}
