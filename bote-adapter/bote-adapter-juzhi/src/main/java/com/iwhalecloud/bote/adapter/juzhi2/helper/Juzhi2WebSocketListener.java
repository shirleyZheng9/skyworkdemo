package com.iwhalecloud.bote.adapter.juzhi2.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 二级聚智 WebSocket 监听器
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class Juzhi2WebSocketListener extends WebSocketListener {
  private static final Logger logger = LoggerFactory.getLogger(Juzhi2WebSocketListener.class);
  /** 日志 ID */
  private final String traceId;
  /** 请求参数，连接建立后作为第一条消息发送 */
  private final Map<String, Object> params;
  /** 消息处理器, 第一个参数为消息文本，第二个参数为消息文本的 JSON 解析结果 */
  private final BiConsumer<String, JsonNode> messageHandler;
  /** 完成回调 */
  private final Consumer<BssException> completionHandler;
  /** 是否已完成 */
  private boolean completed;

  @Override
  public void onOpen(WebSocket webSocket, Response response) {
    logger.debug("websocket opened: traceId={}, code={}", traceId, response.code());
    webSocket.send(JsonUtil.toJsonString(params));
  }

  @Override
  public void onMessage(WebSocket webSocket, String text) {
    logger.debug("Received websocket message: traceId={}, message={}", traceId, text);
    // 解析消息内容
    JsonNode data;
    try {
      data = JsonUtil.readTree(text);
    }
    catch (Exception e) {
      logger.error("Failed to parse websocket message: traceId={}, message={}", traceId, text, e);
      invokeCompletionHandler(e);
      closeWebsocket(webSocket, "parse error");
      return;
    }

    // 检查编码是否成功
    JsonNode header = data.path("header");
    int code = header.path("code").asInt(-1);
    // 失败
    if (code != 0) {
      String errorMsg = header.path("message").asText("");
      logger.error("websocket message error: traceId={}, code={}, error={}, message={}", traceId, code, errorMsg, text);
      invokeCompletionHandler(new BssException("调用聚智接口失败: " + errorMsg));
      closeWebsocket(webSocket, "message error");
    }
    else {
      // 检查会话状态
      int status = header.path("status").asInt(-1);
      // 会话结束
      if (status == 2) {
        invokeCompletionHandler(null);
        closeWebsocket(webSocket, "complete");
        return;
      }
      if (!shouldIgnoreMessage(text, data, status)) {
        messageHandler.accept(text, data);
      }
    }
  }

  /**
   * 检查是否应该忽略消息
   */
  @SuppressWarnings("RedundantIfStatement")
  private boolean shouldIgnoreMessage(String text, JsonNode data, int status) {
    // status=0 表示推理/思考过程，status=1 表示普通消息
    if (status != 0 && status != 1) {
      logger.warn("Invalid message status: traceId={}, message={}", traceId, text);
      return true;
    }
    // 忽略心跳包
    if ("heartbeat".equals(data.path("payload").path("type").asText())) {
      return true;
    }
    return false;
  }

  @Override
  @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
  public void onFailure(WebSocket webSocket, Throwable throwable, @Nullable Response response) {
    if (response == null) {
      logger.error("websocket failure: traceId={}", traceId, throwable);
    }
    else {
      // 读取响应体记录到日志中，方便排查问题
      String body = "";
      try {
        ResponseBody responseBody = response.body(); //NOPMD - suppressed CloseResource - 不需要关闭
        if (responseBody != null) {
          body = responseBody.string();
        }
      }
      catch (Exception e) {
        // 忽略读取响应体失败
      }
      logger.error("websocket failure: traceId={}, code={}, body={}", traceId, response.code(), body, throwable);
    }
    invokeCompletionHandler(throwable);
  }

  @Override
  public void onClosing(WebSocket webSocket, int code, String reason) {
    logger.debug("websocket closed: traceId={}, code={}, reason={}", traceId, code, reason);
    invokeCompletionHandler(null);
  }

  /**
   * 关闭连接
   */
  private void closeWebsocket(WebSocket webSocket, String reason) {
    try {
      webSocket.close(1000, reason);
    }
    catch (Exception e) {
      logger.warn("Failed to close websocket request: traceId={}", traceId);
    }
  }

  /**
   * 调用结束回调
   */
  private void invokeCompletionHandler(@Nullable Throwable throwable) {
    // 避免重复调用
    if (completed) {
      return;
    }
    completed = true;
    if (throwable == null) {
      completionHandler.accept(null);
    }
    else if (throwable instanceof BssException) {
      completionHandler.accept((BssException) throwable);
    }
    else {
      completionHandler.accept(new BssException(throwable));
    }
  }
}
