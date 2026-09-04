package com.iwhalecloud.bote.adapter.panzhi.service;

import com.iwhalecloud.bote.adapter.panzhi.dto.SpeechRecognitionDTO;
import com.iwhalecloud.bote.adapter.panzhi.helper.PanzhiSpeechRecognitionHelper;
import com.iwhalecloud.bote.adapter.panzhi.helper.SpeechRecognitionWebSocketListener;
import com.iwhalecloud.bote.adapter.panzhi.util.PanzhiAuthUtil;
import com.iwhalecloud.bote.dto.asr.RealtimeAsrResultDTO;
import com.iwhalecloud.bote.service.asr.realtime.RealtimeAsrHandler;
import java.util.Map;
import java.util.function.Consumer;
import okhttp3.WebSocket;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 磐智实时语音识别服务实现
 *
 * @author qian.sisheng
 * @since 2026-01-14
 */
public class PanzhiRealtimeAsrHandler implements RealtimeAsrHandler {

  private static final Logger logger = LoggerFactory.getLogger(PanzhiRealtimeAsrHandler.class);

  private final Consumer<RealtimeAsrResultDTO> onMessage;
  private final Consumer<Throwable> onError;
  private WebSocket webSocket;
  private boolean isFirstMessage = true;
  private volatile boolean started = false;
  private String csid;

  public PanzhiRealtimeAsrHandler(Consumer<RealtimeAsrResultDTO> onMessage, Consumer<Throwable> onError) {
    this.onMessage = onMessage;
    this.onError = onError;
  }

  @Override
  public void start() {
    if (started) {
      return;
    }
    Map<String, String> authHeaders = PanzhiAuthUtil.generateAuthHeaders();
    this.csid = MapUtils.getString(authHeaders, "csid");

    SpeechRecognitionWebSocketListener listener = new SpeechRecognitionWebSocketListener(csid, this::handleMessage, this::handleError);

    this.webSocket = PanzhiSpeechRecognitionHelper.createWebSocketConnection(authHeaders, listener);
    this.started = true;
    this.isFirstMessage = true;
  }

  @Override
  public void sendAudio(byte[] pcmData, long startTs, long endTs) {
    if (!started || webSocket == null) {
      return;
    }
    PanzhiSpeechRecognitionHelper.sendAudioData(webSocket, pcmData, false, isFirstMessage);
    isFirstMessage = false;
  }

  @Override
  public void stop() {
    if (started && webSocket != null) {
      PanzhiSpeechRecognitionHelper.sendAudioData(webSocket, null, true, false);
    }
  }

  @Override
  public void close() {
    if (webSocket != null) {
      webSocket.close(1000, "Client closed");
    }
    started = false;
  }

  /**
   * 处理实时语音识别结果
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void handleMessage(SpeechRecognitionDTO dto) {
    if (dto != null) {
      String plainText = dto.getPlainText();
      if (StringUtils.isNotEmpty(plainText)) {
        onMessage.accept(new RealtimeAsrResultDTO(plainText, "replace", true));
      }
    }
  }

  /**
   * 处理错误
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void handleError(Throwable ex) {
    if (ex != null) {
      logger.error("Panzhi realtime speech recognition error: csid={}", csid, ex);
      this.started = false;
      if (onError != null) {
        onError.accept(ex);
      }
    }
  }
}
