package com.iwhalecloud.bote.sandbox.api;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.sandbox.config.SandboxEngineProperties;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileDeleteResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileReadResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileSearchResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileWriteResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunRequest;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunResult;
import com.iwhalecloud.bote.sandbox.dto.UserSandboxBinding;
import com.iwhalecloud.bote.sandbox.pool.SandboxBackendFactory;
import com.iwhalecloud.bote.sandbox.runtime.SandboxRuntime;
import com.iwhalecloud.bote.sandbox.session.UserSandboxBindingRepository;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 沙箱客户端
 *
 * @author bianjp
 * @since 2026-05-08
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class SandboxClient {
  private static final Logger logger = LoggerFactory.getLogger(SandboxClient.class);
  private static final SandboxEngineProperties sandboxEngineProperties = SpringUtil.getBean(SandboxEngineProperties.class);
  private static final SandboxBackendFactory sandboxFactory = SpringUtil.getBean(SandboxBackendFactory.class);
  private static final UserSandboxBindingRepository sandboxRepository = SpringUtil.getBean(UserSandboxBindingRepository.class);

  /** 用户 ID */
  private final Long userId;
  /** 环境变量 */
  private Map<String, String> env;
  /** 沙箱运行时 */
  private SandboxRuntime runtime;
  /** 沙箱 ID */
  @Getter
  private String sandboxId;

  public SandboxClient(Long userId) {
    this.userId = userId;
  }

  /**
   * 初始化沙箱（获取已有沙箱，或创建新沙箱）
   */
  public void init(@Nullable Map<String, String> env) {
    this.env = env;

    // 检查是否已有沙箱
    UserSandboxBinding binding = sandboxRepository.get(userId);
    if (binding != null) {
      runtime = sandboxFactory.attachSandbox(binding.getSandboxId());
      if (runtime != null) {
        // 检查沙箱健康状态，健康时续期，不健康时销毁
        if (runtime.isHealthy()) {
          runtime.renew(sandboxEngineProperties.getSession().getIdleTimeout());
        }
        else {
          runtime.destroy();
          runtime = null;
        }
      }

      // 删除绑定
      if (runtime == null) {
        sandboxRepository.remove(userId);
      }
    }

    // 创建新沙箱
    if (runtime == null) {
      runtime = sandboxFactory.createSandbox(null);
      sandboxRepository.put(userId, new UserSandboxBinding(runtime.getSandboxId()));
    }
    sandboxId = runtime.getSandboxId();
  }

  /**
   * 续期沙箱
   */
  public void renew() {
    runtime.renew(sandboxEngineProperties.getSession().getIdleTimeout());
  }

  /**
   * 执行 shell 命令
   */
  public SandboxRunResult execute(SandboxRunRequest request) {
    Assert.hasLength(request.getCommand(), "command 不能为空");
    if (request.getEnv() == null) {
      request.setEnv(env);
    }
    try {
      return runtime.execute(request);
    }
    catch (Exception e) {
      logger.error("Execute command in sandbox failed: userId={}, sandboxId={}, command={}", userId, sandboxId, request.getCommand(), e);
      String errorMessage = ExpUtil.getMsg(e);
      return SandboxRunResult.builder()
        .success(false)
        .exitCode(1)
        .errorMessage(errorMessage)
        .stderr(errorMessage)
        .build();
    }
  }

  /**
   * 执行 shell 命令，流式处理命令输出
   */
  public void executeWithHandlers(SandboxRunRequest request, SandboxOutputHandlers handlers) {
    Assert.hasLength(request.getCommand(), "command 不能为空");
    if (request.getEnv() == null) {
      request.setEnv(env);
    }
    long startMs = System.currentTimeMillis();
    try {
      runtime.executeWithHandlers(request, handlers, startMs);
    }
    catch (Exception e) {
      logger.error("Execute command with handlers in sandbox failed: userId={}, sandboxId={}, command={}", userId, sandboxId, request.getCommand(), e);
      handlers.onStderr(e.getClass().getSimpleName() + ": " + e.getMessage());
      handlers.onComplete(System.currentTimeMillis() - startMs, false);
    }
  }

  /**
   * 读取文件内容，只支持文本文件
   */
  public SandboxFileReadResult readFile(String path) {
    return readFile(path, null);
  }

  /**
   * 读取文件内容，只支持文本文件
   */
  public SandboxFileReadResult readFile(String path, @Nullable String encoding) {
    Assert.hasLength(path, "path 不能为空");
    try {
      return runtime.readFile(path, encoding);
    }
    catch (Exception e) {
      logger.error("Read file in sandbox failed: userId={}, sandboxId={}, path={}", userId, sandboxId, path, e);
      return SandboxFileReadResult.fail(ExpUtil.getMsg(e));
    }
  }

  /**
   * 流式读取文件内容，支持二进制文件、大文件
   */
  public InputStream readFileStream(String path) {
    Assert.hasLength(path, "path 不能为空");
    return runtime.readFileStream(path);
  }

  /**
   * 写入文件
   */
  public SandboxFileWriteResult writeFile(String path, Object data) {
    return writeFile(path, data, null);
  }

  /**
   * 写入文件
   */
  public SandboxFileWriteResult writeFile(String path, Object data, @Nullable Integer mode) {
    Assert.hasLength(path, "path 不能为空");
    Assert.notNull(data, "data 不能为空");
    try {
      return runtime.writeFile(path, data, mode);
    }
    catch (Exception e) {
      logger.error("Write file in sandbox failed: userId={}, sandboxId={}, path={}", userId, sandboxId, path, e);
      return SandboxFileWriteResult.fail(e.getMessage());
    }
  }

  /**
   * 搜索文件
   */
  public SandboxFileSearchResult searchFiles(String path, @Nullable String pattern) {
    Assert.hasLength(path, "path 不能为空");
    try {
      return runtime.searchFiles(path, pattern);
    }
    catch (Exception e) {
      logger.error("Search files in sandbox failed: userId={}, sandboxId={}, path={}", userId, sandboxId, path, e);
      return SandboxFileSearchResult.fail(e.getMessage());
    }
  }

  /**
   * 删除文件
   */
  public SandboxFileDeleteResult deleteFiles(List<String> paths) {
    Assert.notEmpty(paths, "paths 不能为空");
    try {
      return runtime.deleteFiles(paths);
    }
    catch (Exception e) {
      logger.error("Delete files in sandbox failed: userId={}, sandboxId={}, paths={}", userId, sandboxId, paths, e);
      return SandboxFileDeleteResult.fail(ExpUtil.getMsg(e));
    }
  }
}
