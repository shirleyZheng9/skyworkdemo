package com.iwhalecloud.bote.adapter.panzhi.helper;

import com.iwhalecloud.bote.adapter.panzhi.dto.SpeechRecognitionDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import java.util.function.Consumer;

/**
 * 实时语音转写 WebSocket 监听器
 *
 * @author qian.sisheng
 * @since 2025-01-14
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class SpeechRecognitionWebSocketListener extends WebSocketListener {
  private static final Logger logger = LoggerFactory.getLogger(SpeechRecognitionWebSocketListener.class);
  /** 会话 ID */
  private final String csid;
  /** 消息处理器，接收解析后的结果对象 */
  private final Consumer<SpeechRecognitionDTO> messageHandler;
  /** 完成回调 */
  private final Consumer<BssException> completionHandler;
  /** 是否已完成 */
  private boolean completed;

  @Override
  public void onOpen(WebSocket webSocket, Response response) {
    logger.debug("Speech recognition WebSocket connection established: csid={}, code={}", csid, response.code());
  }

  @Override
  public void onMessage(WebSocket webSocket, String text) {
    logger.debug("Received speech recognition message: csid={}, message={}", csid, text);

    // 解析消息内容
    SpeechRecognitionDTO result;
    try {
      result = JsonUtil.parseJson(text, SpeechRecognitionDTO.class);
    }
    catch (Exception e) {
      logger.error("Failed to parse speech recognition message: csid={}, message={}", csid, text, e);
      invokeCompletionHandler(new BssException("解析语音转写消息失败: " + e.getMessage(), e));
      closeWebSocket(webSocket, "parse error");
      return;
    }

    // 检查结果是否为null
    if (result == null) {
      logger.error("Speech recognition returned null result: csid={}, message={}", csid, text);
      invokeCompletionHandler(new BssException("语音转写返回空结果"));
      closeWebSocket(webSocket, "null result");
      return;
    }

    // 检查是否有错误
    if (result.hasError()) {
      String errorMsg = result.getErrorMsg();
      logger.error("Speech recognition returned error: csid={}, errorCode={}, errorMsg={}, message={}", csid, result.getErrorCode(), errorMsg, text);
      invokeCompletionHandler(new BssException("语音转写失败: " + errorMsg));
      closeWebSocket(webSocket, "service error");
      return;
    }

    // 处理成功消息
    try {
      messageHandler.accept(result);

      // 检查是否结束
      if (Boolean.TRUE.equals(result.getEndFlag())) {
        logger.debug("Speech recognition session ended: csid={}", csid);
        invokeCompletionHandler(null);
        closeWebSocket(webSocket, "completed");
      }
    }
    catch (Exception e) {
      logger.error("Exception occurred while processing speech recognition message: csid={}, message={}", csid, text, e);
      invokeCompletionHandler(new BssException("处理语音转写消息失败: " + e.getMessage(), e));
      closeWebSocket(webSocket, "handler error");
    }
  }

  @Override
  @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
  public void onFailure(WebSocket webSocket, Throwable throwable, @Nullable Response response) {
    String responseInfo = "";
    if (response != null) {
      responseInfo = String.format(", response code: %d, message: %s", response.code(), response.message());
    }
    logger.error("Speech recognition WebSocket connection failed: csid={}{}", csid, responseInfo, throwable);
    String errorMessage = "语音转写WebSocket连接失败: " + throwable.getMessage();
    invokeCompletionHandler(new BssException(errorMessage, throwable));
    closeWebSocket(webSocket, "connection failed");
  }

  @Override
  public void onClosing(WebSocket webSocket, int code, String reason) {
    logger.debug("Speech recognition WebSocket connection closing: csid={}, code={}, reason={}", csid, code, reason);
    webSocket.close(1000, null);
  }

  @Override
  public void onClosed(WebSocket webSocket, int code, String reason) {
    logger.debug("Speech recognition WebSocket connection closed: csid={}, code={}, reason={}", csid, code, reason);

    // 如果连接关闭时还没有调用完成回调，则调用它
    if (!completed) {
      invokeCompletionHandler(new BssException("WebSocket连接意外关闭"));
    }
  }

  /**
   * 关闭 WebSocket连接
   */
  private void closeWebSocket(WebSocket webSocket, String reason) {
    try {
      webSocket.close(1000, reason);
    }
    catch (Exception e) {
      logger.warn("Exception occurred while closing WebSocket connection: csid={}, reason={}", csid, reason, e);
    }
  }

  /**
   * 调用完成回调
   */
  private void invokeCompletionHandler(@Nullable Throwable throwable) {
    if (completed) {
      return;
    }
    completed = true;
    try {
      BssException exception = null;
      if (throwable != null) {
        if (throwable instanceof BssException) {
          exception = (BssException) throwable;
        }
        else {
          exception = new BssException(throwable.getMessage(), throwable);
        }
      }
      completionHandler.accept(exception);
    }
    catch (Exception e) {
      logger.error("Exception occurred while invoking completion callback: csid={}", csid, e);
    }
  }
}
