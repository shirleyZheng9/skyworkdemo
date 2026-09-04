package com.iwhalecloud.bote.llm.client.util;

import com.iwhalecloud.bote.llm.client.dto.ServerSentEvent;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okio.BufferedSource;
import org.springframework.lang.Nullable;

/**
 * SSE 响应读取器
 *
 * @author bianjp
 * @since 2025-12-17
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
class ServerSentEventReader {
  /** 请求地址 */
  private final HttpUrl url;
  /** 请求调用器 */
  private final Call call;
  /** 响应源 */
  private final BufferedSource source;
  /** 当前事件 ID */
  private String id = null;
  /** 当前事件名称 */
  private String event = null;
  /** 当前事件数据 */
  private final StringBuilder data = new StringBuilder();

  /**
   * 读取 SSE 响应
   *
   * @param handler 事件处理器
   */
  public void read(Consumer<ServerSentEvent> handler) {
    String line;
    try {
      // null 表示响应结束
      while ((line = source.readUtf8Line()) != null) {
        // 空行表示一个事件结束
        if (line.isEmpty()) {
          ServerSentEvent eventItem = buildEvent();
          if (eventItem != null) {
            try {
              handler.accept(eventItem);
            }
            catch (BssException e) {
              ModelHttpClient.logger.error("Failed to process SSE event: url={}, event={}, error={}", url, eventItem.data(), e.getMessage());
              throw e;
            }
            catch (Exception e) {
              ModelHttpClient.logger.error("Failed to process SSE event: url={}, event={}", url, eventItem.data(), e);
              throw new BssException("处理 SSE 事件失败: " + e.getMessage(), e);
            }
          }
        }
        else {
          processLine(line);
        }
      }
    }
    catch (IOException e) {
      handleException(e);
    }
  }

  /**
   * 处理读取响应的异常
   */
  private void handleException(IOException e) {
    if (call.isCanceled() && "Socket closed".equals(e.getMessage())) {
      ModelHttpClient.logger.warn("Failed to read SSE output, request cancelled: url={}", url, e);
      throw new BssException("读取 SSE 响应失败: 请求被取消", e);
    }
    else {
      ModelHttpClient.logger.warn("Failed to read SSE output: url={}", url, e);
      throw new BssException("读取 SSE 响应失败: " + e.getMessage(), e);
    }
  }

  /**
   * 构造事件
   */
  @Nullable
  private ServerSentEvent buildEvent() {
    ServerSentEvent eventItem = null;
    // 数据是必填的，忽略没有数据的事件
    if (!data.isEmpty()) {
      eventItem = new ServerSentEvent(id, event, data.toString());
      ModelHttpClient.logger.trace("Received SSE event: url={}, id={}, event={}, data={}", url, id, event, eventItem.data());
    }
    id = null;
    event = null;
    data.setLength(0);
    return eventItem;
  }

  /**
   * 处理一行输出
   */
  private void processLine(String line) {
    // 参考 org.springframework.http.codec.ServerSentEventHttpMessageReader#buildEvent
    if (line.startsWith("id:")) {
      id = line.substring("id:".length()).stripLeading();
    }
    else if (line.startsWith("event:")) {
      event = line.substring("event:".length()).stripLeading();
    }
    else if (line.startsWith("data:")) {
      // 一个事件中允许有多个 data 行，需用换行符连接起来
      if (!data.isEmpty()) {
        data.append("\n");
      }
      data.append(line.substring("data:".length()).stripLeading());
    }
    // 忽略注释和不需要支持的 retry, 其它未知属性打印警告日志
    else if (!line.startsWith(":") && !line.startsWith("retry:")) {
      ModelHttpClient.logger.warn("Invalid SSE output: url={}, line={}", url, line);
    }
  }
}
