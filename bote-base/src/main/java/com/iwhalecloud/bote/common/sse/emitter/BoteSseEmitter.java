package com.iwhalecloud.bote.common.sse.emitter;

import java.io.IOException;
import java.util.Set;
import lombok.Getter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 自定义 SSE 触发器
 *
 * <p>记录状态、发送消息时间</p>
 *
 * @author bianjp
 * @since 2025-11-13
 */
public class BoteSseEmitter extends SseEmitter {
  /** 是否已关闭 */
  @Getter
  private volatile boolean closed = false;
  /** 最后发送时间 */
  @Getter
  private volatile long lastSentTime = System.currentTimeMillis();
  /** 客户端 ID */
  @Nullable
  private final String clientId;
  /** 是否是会话接口，会话接口需要特殊处理客户端断开连接的情况（继续执行而非报错） */
  @Getter
  private final boolean chatApi;

  public BoteSseEmitter(Long timeout, @Nullable String clientId, boolean chatApi) {
    super(timeout);
    this.clientId = clientId;
    this.chatApi = chatApi;
  }

  /**
   * 获取客户端 ID
   */
  @Nullable
  public String getClientId() {
    return clientId;
  }

  @Override
  protected void extendResponse(ServerHttpResponse outputMessage) {
    super.extendResponse(outputMessage);
    // 避免 Nginx 缓冲
    //noinspection UastIncorrectHttpHeaderInspection
    outputMessage.getHeaders().set("X-Accel-Buffering", "no");
    super.onCompletion(() -> this.closed = true);
  }

  @Override
  public void send(SseEventBuilder builder) throws IOException {
    this.lastSentTime = System.currentTimeMillis();
    // 父类方法中使用了 super.send, 导致不会进入当前类中的 send(Set<DataWithMediaType>) 方法
    super.send(builder);
  }

  @Override
  public void send(Set<DataWithMediaType> items) throws IOException {
    this.lastSentTime = System.currentTimeMillis();
    super.send(items);
  }

  @Override
  public void complete() {
    this.closed = true;
    super.complete();
  }

  @Override
  public void completeWithError(Throwable ex) {
    this.closed = true;
    super.completeWithError(ex);
  }
}
