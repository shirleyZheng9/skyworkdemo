package com.iwhalecloud.bote.mcp.client.transport;

import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.mcp.dto.StdioServerParameters;
import com.iwhalecloud.bote.mcp.dto.message.JsonRpcMessage;
import com.iwhalecloud.bote.mcp.util.McpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * MCP 的 stdio 传输协议
 *
 * @author bianjp
 * @since 2025-05-22
 */
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
@SuppressWarnings("PMD.GuardLogStatement")
public class StdioClientTransport implements McpClientTransport {
  private static final Logger logger = LoggerFactory.getLogger(StdioClientTransport.class);

  /** 客户端标识，用于日志中区分不同客户端 */
  private final String clientId;
  /** 服务器参数 */
  private final StdioServerParameters serverParameters;

  /** 标准输出接收器（接收回复） */
  private Sinks.Many<JsonRpcMessage> stdoutSink;
  /** 标准错误接收器 */
  private Sinks.Many<String> stderrSink;
  /** 标准输入接收器（发送请求） */
  private Sinks.Many<JsonRpcMessage> stdinSink;

  /** 标准错误输出的处理器，默认只打印日志 */
  @Setter
  private Consumer<String> stderrHandler;
  /** 服务器进程 */
  private Process process;
  /** 是否正在关闭 */
  private volatile boolean isClosing = false;
  /** 处理标准输出的调度器 */
  private Disposable stdoutDisposable;
  /** 处理标准错误的调度器 */
  private Disposable stderrDisposable;
  /** 处理标准输入的调度器 */
  private Disposable stdinDisposable;
  /** 处理标准输入的线程池 */
  private Scheduler stdinScheduler;

  public StdioClientTransport(String clientId, StdioServerParameters serverParameters) {
    Assert.notNull(serverParameters, "MCP 服务器参数不能为空");
    Assert.notEmpty(serverParameters.getCommand(), "MCP 服务器命令不能为空");
    this.clientId = clientId;
    this.serverParameters = serverParameters;
  }

  /**
   * 启动服务器进程并初始化
   */
  @Override
  public Mono<Void> connect(Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> handler, Runnable disconnectionHandler) {
    return Mono.<Void>fromRunnable(() -> doConnect(handler)).subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * 启动服务器
   */
  @SuppressFBWarnings("COMMAND_INJECTION")
  private void doConnect(Function<Mono<JsonRpcMessage>, Mono<JsonRpcMessage>> handler) {
    // 重连时需要关闭线程
    closeSinks();
    closeSchedulers();
    stdoutSink = Sinks.many().unicast().onBackpressureBuffer();
    stdinSink = Sinks.many().unicast().onBackpressureBuffer();
    stderrSink = Sinks.many().unicast().onBackpressureBuffer();

    stdoutSink.asFlux()
      .flatMap(message -> Mono.just(message)
        .transform(handler)
        .contextWrite(ctx -> ctx.put("observation", "myObservation")))
      .subscribe();
    stderrSink.asFlux().subscribe(error -> {
      if (stderrHandler != null) {
        stderrHandler.accept(error);
      }
      else {
        logger.warn("Received stderr message: client={}, error={}", clientId, error);
      }
    });

    // 创建进程
    ProcessBuilder processBuilder = new ProcessBuilder(serverParameters.getCommand());
    processBuilder.environment().putAll(serverParameters.getEnv());

    // 启动进程
    try {
      logger.trace("Starting stdio command: client={}, command={}", clientId, serverParameters.getCommand());
      process = processBuilder.start();
      // 等待几秒，如果进程异常退出，则报错
      if (process.waitFor(2, TimeUnit.SECONDS)) {
        String error = "";
        try {
          error = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
        }
        catch (Exception e) {
          // 忽略读取错误信息失败
        }
        int exitCode = process.exitValue();
        logger.error("Process exited abnormally: client={}, command={}, code={}, stderr={}", clientId, serverParameters.getCommand(), exitCode, error);
        throw new BssException("启动 MCP 服务器进程失败: code=" + exitCode + ", " + error);
      }
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to start MCP server process: client={}, command={}", clientId, serverParameters.getCommand(), e);
      throw new BssException("启动 MCP 服务器进程失败: " + e.getMessage(), e);
    }

    // 启动处理线程
    startThreads();
  }

  @Override
  public boolean isConnected() {
    return process != null && process.isAlive();
  }

  @Override
  public Mono<Void> sendMessage(JsonRpcMessage message) {
    try {
      // 并发调用 tryEmitNext 会返回 EmitResult.FAIL_NON_SERIALIZED, 使用 emitNext 以实现自动重试
      stdinSink.emitNext(message, EmitFailureHandler.busyLooping(Duration.ofSeconds(10)));
      return Mono.empty();
    }
    catch (Exception e) {
      logger.error("Failed to emit client message", e);
      return Mono.error(new BssException("MCP 客户端消息加入队列失败"));
    }
  }

  /**
   * 启动处理线程
   */
  private void startThreads() {
    Scheduler scheduler = Schedulers.fromExecutor(ThreadPools.getMcp(), true);
    // 处理标准输出
    stdoutDisposable = scheduler.schedule(this::processStdout);
    // 处理标准错误
    stderrDisposable = scheduler.schedule(this::processStderr);
    // 使用一个固定的线程处理标准输入，避免并发调用（进程的 stdio 不支持并发写入）
    stdinScheduler = Schedulers.fromExecutorService(Executors.newSingleThreadExecutor(Thread.ofVirtual().factory()), "mcp-stdin");
    // 处理标准输入
    stdinDisposable = stdinSink.asFlux()
      .publishOn(stdinScheduler)
      .handle(this::processClientMessage)
      .doOnComplete(() -> {
        isClosing = true;
        stdinSink.tryEmitComplete();
      })
      .doOnError(e -> {
        if (!isClosing) {
          logger.error("Error in stdin processing: client={}", clientId, e);
          isClosing = true;
          stdinSink.tryEmitComplete();
        }
      }).subscribe();
  }

  /**
   * 处理进程的标准输出
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void processStdout() {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while (!isClosing && (line = reader.readLine()) != null) {
        try {
          logger.trace("Received stdout message: client={}, message={}", clientId, line);
          JsonRpcMessage message = McpUtil.deserializeMessage(line);
          if (!stdoutSink.tryEmitNext(message).isSuccess()) {
            if (!isClosing) {
              logger.error("Failed to enqueue stdout message: client={}, message={}", clientId, line);
            }
            break;
          }
        }
        catch (Exception e) {
          if (!isClosing) {
            logger.error("Failed to handle stdout line: client={}, line={}", clientId, line, e);
          }
          break;
        }
      }
    }
    catch (Exception e) {
      if (!isClosing) {
        logger.error("Failed to handle stdout: client={}", clientId, e);
      }
    }
    finally {
      isClosing = true;
      stdoutSink.tryEmitComplete();
    }
  }

  /**
   * 处理进程的标准错误
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void processStderr() {
    try (BufferedReader processErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
      String line;
      while (!isClosing && (line = processErrorReader.readLine()) != null) {
        try {
          if (!stderrSink.tryEmitNext(line).isSuccess()) {
            if (!isClosing) {
              logger.error("Failed to enqueue stderr message: client={}, message={}", clientId, line);
            }
            break;
          }
        }
        catch (Exception e) {
          if (!isClosing) {
            logger.error("Failed to handle stderr line: client={}, line={}", clientId, line, e);
          }
          break;
        }
      }
    }
    catch (Exception e) {
      if (!isClosing) {
        logger.error("Failed to handle stderr: client={}", clientId, e);
      }
    }
    finally {
      isClosing = true;
      stderrSink.tryEmitComplete();
    }
  }

  /**
   * 处理客户端请求，写入进程的标准输入
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void processClientMessage(@Nullable JsonRpcMessage message, SynchronousSink<JsonRpcMessage> sink) {
    if (message == null || isClosing) {
      return;
    }
    try {
      String jsonMessage = JsonUtil.toJsonString(message);
      OutputStream os = process.getOutputStream(); //NOPMD - suppressed CloseResource - 进程不会结束，不能关闭输出流
      logger.trace("Sending stdin message: client={}, message={}", clientId, jsonMessage);
      // 已经使用了专用的线程处理，不需要再做同步？
      synchronized (os) {
        os.write(jsonMessage.getBytes(StandardCharsets.UTF_8));
        os.write("\n".getBytes(StandardCharsets.UTF_8));
        os.flush();
      }
      sink.next(message);
    }
    catch (Exception e) {
      logger.error("Failed to process mcp client message: client={}, message={}", clientId, message, e);
      sink.error(new BssException("处理 MCP 客户端消息失败: " + e.getMessage(), e));
    }
  }

  @Override
  public Mono<Void> closeGracefully() {
    isClosing = true;
    logger.debug("Start graceful shutdown: client={}", clientId);
    // 停止接收新消息
    return Mono.fromRunnable(this::closeSinks)
      // 等待当前队列中的消息处理完毕
      .then(Mono.defer(() -> Mono.delay(Duration.ofMillis(100))))
      // 关闭进程
      .then(Mono.fromRunnable(this::closeProcess))
      // 关闭调度器
      .then(Mono.<Void>fromRunnable(this::closeSchedulers))
      .doOnSuccess(v -> logger.debug("Graceful shutdown completed: client={}", clientId))
      .doOnError(e -> logger.error("Failed to graceful shutdown: client={}", clientId, e))
      .subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * 关闭进程
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void closeProcess() {
    if (process == null) {
      logger.warn("Process not started: client={}", clientId);
      return;
    }
    logger.debug("Killing process: client={}", clientId);
    // 杀掉进程
    process.destroy();
    // 等待进程结束
    try {
      if (process.waitFor(5, TimeUnit.SECONDS)) {
        int exitCode = process.exitValue();
        if (exitCode != 0) {
          logger.warn("Process terminated abnormally: client={}, code={}", clientId, exitCode);
        }
      }
      else {
        logger.warn("Process did not terminate in 5 seconds, killing it forcibly: client={}", clientId);
        // 如果进程还未结束，强制杀掉
        process.destroyForcibly();
      }
    }
    catch (InterruptedException e) {
      logger.warn("Interrupted while waiting for process to terminate: client={}", clientId);
      process.destroyForcibly();
    }
  }

  /**
   * 关闭接收器
   */
  private void closeSinks() {
    if (stdinSink != null) {
      stdinSink.tryEmitComplete();
    }
    if (stdoutSink != null) {
      stdoutSink.tryEmitComplete();
    }
    if (stderrSink != null) {
      stderrSink.tryEmitComplete();
    }
  }

  /**
   * 关闭调度器
   */
  private void closeSchedulers() {
    if (stdinScheduler != null) {
      stdinScheduler.dispose();
    }
    if (stdinDisposable != null) {
      stdinDisposable.dispose();
    }
    if (stdoutDisposable != null) {
      stdoutDisposable.dispose();
    }
    if (stderrDisposable != null) {
      stderrDisposable.dispose();
    }
  }

}
