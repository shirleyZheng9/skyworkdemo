package com.iwhalecloud.bote.adapter.panzhi.helper;

import com.iwhalecloud.bote.adapter.panzhi.util.AudioFormatConverterUtil;
import com.iwhalecloud.bote.adapter.panzhi.util.PanzhiAuthUtil;
import com.iwhalecloud.bote.adapter.panzhi.dto.SpeechRecognitionDTO;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.springframework.lang.Nullable;

/**
 * 磐智实时语音转写工具类
 *
 * @author qian.sisheng
 * @since 2025-09-12
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class PanzhiSpeechRecognitionHelper {
  private static final Logger logger = LoggerFactory.getLogger(PanzhiSpeechRecognitionHelper.class);

  /** 音频分块大小（字节）*/
  private static final int AUDIO_CHUNK_SIZE = 1280;
  /** 音频发送间隔（毫秒）*/
  private static final int AUDIO_SEND_INTERVAL_MS = 40;
  /** 语音识别超时时间（分钟）*/
  private static final int RECOGNITION_TIMEOUT_MINUTES = 5;

  private PanzhiSpeechRecognitionHelper() {
  }

  /**
   * 同步方式进行语音转写（仅支持plain结果类型）
   *
   * @param file 音频文件
   * @return 转写结果（plain文本）
   */
  public static String recognizeSpeech(@Nullable File file) {
    if (file == null || !file.exists()) {
      throw new BssException("音频文件不存在或为空");
    }
    final RecognitionContext context = new RecognitionContext();
    WebSocket webSocket = null;
    try {
      // 创建WebSocket连接
      webSocket = createWebSocketConnection(context);
      // 处理音频数据
      processAudioFile(file, webSocket, context);
      // 等待识别完成
      return waitForRecognitionResult(context);
    }
    catch (Exception e) {
      logger.error("Speech recognition exception", e);
      throw new BssException("语音转写异常, msg=" + e.getMessage(), e);
    }
    finally {
      // 确保WebSocket连接被正确关闭
      closeWebSocketSafely(webSocket);
    }
  }

  /**
   * 创建WebSocket连接
   *
   * @param authHeaders 鉴权头部信息
   * @param listener WebSocket监听器
   * @return WebSocket实例
   */
  public static WebSocket createWebSocketConnection(Map<String, String> authHeaders, WebSocketListener listener) {
    Request.Builder requestBuilder = new Request.Builder().url(PanzhiAuthUtil.getWebSocketUrl());
    if (authHeaders != null) {
      authHeaders.forEach(requestBuilder::addHeader);
    }
    return ModelHttpClient.getClient().newWebSocket(requestBuilder.build(), listener);
  }

  /**
   * 创建WebSocket连接
   */
  private static WebSocket createWebSocketConnection(RecognitionContext context) {
    Map<String, String> authHeaders = PanzhiAuthUtil.generateAuthHeaders();
    SpeechRecognitionWebSocketListener listener = new SpeechRecognitionWebSocketListener(MapUtils.getString(authHeaders, "csid"),
      context.getResultHandler(), context.getCompletionHandler());
    return createWebSocketConnection(authHeaders, listener);
  }

  /**
   * 处理音频文件
   */
  private static void processAudioFile(File file, WebSocket webSocket, RecognitionContext context) throws Exception {
    byte[] allAudioBytes = AudioFormatConverterUtil.convertToRawBytes(file);
    if (allAudioBytes == null || allAudioBytes.length == 0) {
      logger.warn("Audio file is empty");
      return;
    }
    List<byte[]> chunkedAudioDataList = chunkAudioData(allAudioBytes);
    sendAudioData(webSocket, chunkedAudioDataList, context.shouldStop);
    SseUtil.requestListener.accept(webSocket);
  }

  /**
   * 等待识别结果
   */
  private static String waitForRecognitionResult(RecognitionContext context) {
    try {
      boolean success = context.latch.await(RECOGNITION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
      if (!success) {
        throw new BssException("语音转写超时");
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("语音转写被中断, msg=" + e.getMessage(), e);
    }
    if (context.error.get() != null) {
      throw context.error.get();
    }
    if (context.result.get() == null) {
      throw new BssException("未找到有效的文件解析内容结果");
    }
    return context.result.get();
  }

  /**
   * 安全关闭WebSocket连接
   */
  private static void closeWebSocketSafely(@Nullable WebSocket webSocket) {
    if (webSocket != null) {
      try {
        webSocket.close(1000, "Normal closure");
      }
      catch (Exception e) {
        logger.warn("Exception occurred while closing WebSocket connection", e);
      }
    }
  }

  /**
   * 发送音频数据
   *
   * @param webSocket WebSocket连接
   * @param audioData 音频数据（PCM格式）
   * @param isEnd 是否为最后一帧
   * @param isFirstMessage 是否为第一条消息（需要包含sessionParam）
   * @throws BssException 发送失败时抛出
   */
  public static void sendAudioData(@Nullable WebSocket webSocket, @Nullable byte[] audioData, boolean isEnd, boolean isFirstMessage) {
    if (webSocket == null) {
      throw new BssException("WebSocket连接不能为空");
    }

    try {
      // 构建消息体
      Map<String, Object> message = new HashMap<>(3);

      // 第一条消息需要包含sessionParam
      if (isFirstMessage) {
        Map<String, Object> sessionParams = PanzhiAuthUtil.getSessionParams();
        message.put("sessionParam", sessionParams);
        logger.debug("Adding session parameters to first message: {}", sessionParams);
      }

      if (audioData != null && audioData.length > 0) {
        // Base64编码音频数据
        String encodedAudio = Base64.encodeBase64String(audioData);
        message.put("samples", encodedAudio);
        logger.debug("Preparing to send audio data: size={} bytes, isEnd={}, isFirstMessage={}", audioData.length, isEnd, isFirstMessage);
      }
      else {
        logger.debug("Preparing to send end marker: isEnd={}, isFirstMessage={}", isEnd, isFirstMessage);
      }
      message.put("endFlag", isEnd);

      // 发送消息
      String messageJson = JsonUtil.toJsonString(message);
      boolean success = webSocket.send(messageJson);

      if (!success) {
        throw new BssException("WebSocket发送消息失败，可能连接已关闭");
      }

    }
    catch (Exception e) {
      int dataSize = audioData != null ? audioData.length : 0;
      logger.error("Exception sending audio data: endFlag={}, dataSize={} bytes, isFirstMessage={}", isEnd, dataSize, isFirstMessage, e);
      throw new BssException("发送音频数据失败", e);
    }
  }

  /**
   * 异步发送音频数据列表
   *
   * @param webSocket WebSocket连接
   * @param audioDataList 音频数据列表
   * @param shouldStop 停止标志
   */
  private static void sendAudioData(WebSocket webSocket, @Nullable List<byte[]> audioDataList, AtomicReference<Boolean> shouldStop) {
    if (audioDataList == null || audioDataList.isEmpty()) {
      logger.warn("Audio data list is empty, sending end marker directly");
      return;
    }
    int totalChunks = audioDataList.size();
    logger.debug("Starting to send audio data, {} chunks total, sending one chunk every {}ms", totalChunks, AUDIO_SEND_INTERVAL_MS);

    try {
      for (int i = 0; i < totalChunks; i++) {
        // 检查是否应该停止发送
        if (shouldStop.get()) {
          logger.debug("Terminating audio data transmission: sent {}/{} chunks", i, totalChunks);
          return;
        }

        byte[] audioData = audioDataList.get(i);
        boolean isEnd = i == totalChunks - 1;
        boolean isFirstMessage = i == 0;

        try {
          sendAudioData(webSocket, audioData, isEnd, isFirstMessage);
          logger.debug("Sending audio data chunk: index={}/{}, size={}, isEnd={}", i + 1, totalChunks, audioData.length, isEnd);
        }
        catch (Exception e) {
          logger.error("Failed to send audio data chunk {}", i + 1, e);
          throw e;
        }

        // 如果不是最后一帧，等待指定间隔再发送下一帧
        if (!isEnd) {
          try {
            Thread.sleep(AUDIO_SEND_INTERVAL_MS);
          }
          catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Audio transmission interrupted");
            return;
          }
        }
      }
      logger.debug("Audio data transmission completed, sent {} chunks total", totalChunks);
    }
    catch (Exception e) {
      logger.error("Failed to send audio data", e);
      // 发送失败时尝试发送结束标记
      sendEndMarkerSafely(webSocket, shouldStop);
      throw new BssException("音频数据发送失败", e);
    }
  }

  /**
   * 安全发送结束标记
   */
  private static void sendEndMarkerSafely(WebSocket webSocket, AtomicReference<Boolean> shouldStop) {
    try {
      if (!shouldStop.get()) {
        sendAudioData(webSocket, null, true, false);
        logger.debug("End marker sent");
      }
    }
    catch (Exception ex) {
      logger.error("Failed to send end marker", ex);
    }
  }

  /**
   * 将音频数据分块
   *
   * @param allAudioBytes 原始音频数据字节数组
   * @return 分块后的音频数据列表
   */
  private static List<byte[]> chunkAudioData(@Nullable byte[] allAudioBytes) {
    if (allAudioBytes == null || allAudioBytes.length == 0) {
      return new ArrayList<>();
    }
    int totalLength = allAudioBytes.length;
    // 计算分块数
    int expectedChunks = (totalLength + AUDIO_CHUNK_SIZE - 1) / AUDIO_CHUNK_SIZE;
    List<byte[]> chunkedList = new ArrayList<>(expectedChunks);
    // 分块
    for (int offset = 0; offset < totalLength; offset += AUDIO_CHUNK_SIZE) {
      int endIndex = Math.min(offset + AUDIO_CHUNK_SIZE, totalLength);
      byte[] chunk = Arrays.copyOfRange(allAudioBytes, offset, endIndex);
      chunkedList.add(chunk);
    }
    logger.debug("Audio data chunking completed: total size={} bytes, chunk count={}, chunk size={} bytes", totalLength, chunkedList.size(), AUDIO_CHUNK_SIZE);
    return chunkedList;
  }

  /**
   * 语音识别上下文，封装识别过程中的状态信息
   */
  private static final class RecognitionContext {
    /** 识别结果 */
    final AtomicReference<String> result = new AtomicReference<>();
    /** 错误信息 */
    final AtomicReference<BssException> error = new AtomicReference<>();
    /** 等待锁 */
    final CountDownLatch latch = new CountDownLatch(1);
    /** 累积plain文本结果 */
    final StringBuilder plainTextBuilder = new StringBuilder();
    /** 是否应该停止 */
    final AtomicReference<Boolean> shouldStop = new AtomicReference<>(false);

    Consumer<BssException> getCompletionHandler() {
      return e -> {
        error.set(e);
        shouldStop.set(true);
        latch.countDown();
      };
    }

    Consumer<SpeechRecognitionDTO> getResultHandler() {
      return speechResult -> {
        if (speechResult != null) {
          // 累积plain文本结果
          String plainText = speechResult.getPlainText();
          if (StringUtils.isNotBlank(plainText)) {
            plainTextBuilder.append(plainText);
          }
          // 检查是否结束
          if (Boolean.TRUE.equals(speechResult.getEndFlag())) {
            result.set(plainTextBuilder.toString());
            latch.countDown();
          }
        }
      };
    }
  }
}
