package com.iwhalecloud.bote.service.asr.realtime.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.asr.RealtimeAsrResultDTO;
import com.iwhalecloud.bote.dto.asr.takeai.TakeAiAudioDTO;
import com.iwhalecloud.bote.dto.asr.takeai.TakeAiHeaderDTO;
import com.iwhalecloud.bote.dto.asr.takeai.TakeAiMessageDTO;
import com.iwhalecloud.bote.dto.asr.takeai.TakeAiPayloadDTO;
import com.iwhalecloud.bote.dto.asr.takeai.TakeAiResultDTO;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bote.service.asr.realtime.RealtimeAsrHandler;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TakeAi WebSocket 客户端
 *
 * @author qian.sisheng
 * @since 2026-01-09
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class TakeAiRealtimeAsrHandler implements RealtimeAsrHandler {

  private static final Logger logger = LoggerFactory.getLogger(TakeAiRealtimeAsrHandler.class);

  /** 命名空间: 语音转写 */
  private static final String NAMESPACE_SPEECH_TRANSCRIBER = "SpeechTranscriber";
  /** 指令: 开始转写 */
  private static final String NAME_START_TRANSCRIPTION = "StartTranscription";
  /** 指令: 停止转写 */
  private static final String NAME_STOP_TRANSCRIPTION = "StopTranscription";
  /** 事件: 转写已开始 */
  private static final String NAME_TRANSCRIPTION_STARTED = "TranscriptionStarted";
  /** 事件: 句子结束 */
  private static final String NAME_SENTENCE_END = "SentenceEnd";
  /** 事件: 转写结果变化 */
  private static final String NAME_TRANSCRIPTION_RESULT_CHANGED = "TranscriptionResultChanged";
  /** 事件: 转写完成 */
  private static final String NAME_TRANSCRIPTION_COMPLETED = "TranscriptionCompleted";
  /** 事件: 任务失败 */
  private static final String NAME_TASK_FAILED = "TaskFailed";

  /** WebSocket 连接地址 */
  private final String url;
  /** 认证 Token */
  private final String token;
  /** 应用 Key */
  private final String appKey;
  /** 消息回调，用于接收识别结果 */
  private final Consumer<RealtimeAsrResultDTO> onMessage;
  /** 错误回调，用于接收异常信息 */
  private final Consumer<Throwable> onError;
  /** 关闭闭锁，用于等待连接关闭 */
  private final CountDownLatch closeLatch = new CountDownLatch(1);
  /** 连接闭锁，用于等待连接建立 */
  private final CountDownLatch connectLatch = new CountDownLatch(1);
  /** WebSocket 实例 */
  private WebSocket webSocket;
  /** 任务 ID */
  private String taskId;
  /** 连接是否成功 */
  private volatile boolean connected = false;
  /** 启动状态 */
  private volatile boolean started = false;
  /** 连接异常 */
  private volatile Throwable connectError;

  /**
   * 构造函数
   *
   * @param onMessage 消息回调
   * @param onError 错误回调
   */
  public TakeAiRealtimeAsrHandler(Consumer<RealtimeAsrResultDTO> onMessage, Consumer<Throwable> onError) {
    this.url = SystemParameter.VIDEO_OCR_API_URL.getValueFromDb();
    this.token = SystemParameter.TAKE_AI_TOKEN.getValueFromDb();
    this.appKey = SystemParameter.TAKE_AI_APP_KEY.getValueFromDb();
    this.onMessage = onMessage;
    this.onError = onError;
  }

  /**
   * 启动 WebSocket 连接并发送开始指令
   */
  @Override
  public void start() {
    if (started) {
      return;
    }
    Request request = new Request.Builder().url(url).build();
    try {
      OkHttpClient client = ModelHttpClient.getClient().newBuilder().pingInterval(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
      this.webSocket = client.newWebSocket(request, new TakeAiWebSocketListener());
      // 等待连接建立
      if (!connectLatch.await(10, TimeUnit.SECONDS)) {
        throw new BssException("TakeAi 连接超时");
      }
      if (connectError != null) {
        throw new BssException("TakeAi 连接失败", connectError);
      }
      if (!connected) {
        throw new BssException("TakeAi 连接失败");
      }
      String startJson = buildStartRequest();
      boolean sent = webSocket.send(startJson);
      if (!sent) {
        throw new BssException("发送启动命令失败");
      }
      logger.debug("TakeAi Start command sent, taskId={}", taskId);
      this.started = true;
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("TakeAi 连接中断", e);
    }
    catch (Exception e) {
      logger.error("TakeAi Start failed", e);
      throw new BssException("TakeAi 启动失败", e);
    }
  }

  /**
   * 构建开始请求 JSON
   *
   * @return 开始请求的 JSON 字符串
   */
  private String buildStartRequest() {
    // 构造请求头
    TakeAiHeaderDTO header = new TakeAiHeaderDTO();
    header.setAppkey(appKey);
    header.setToken(token);
    header.setNamespace(NAMESPACE_SPEECH_TRANSCRIBER);
    header.setName(NAME_START_TRANSCRIPTION);
    header.setMessageId(generateUuid());
    this.taskId = generateUuid();
    header.setTaskId(this.taskId);
    // 构造请求体
    TakeAiPayloadDTO payload = new TakeAiPayloadDTO();
    payload.setFormat("pcm");
    payload.setSampleRate(16000);
    payload.setDelay(3000);
    payload.setEnableCorrection(true);
    payload.setEnableSemanticSentenceDetection(true);
    payload.setMaxSegmentDurationMs(30000);
    updatePayload(payload);
    TakeAiMessageDTO request = new TakeAiMessageDTO();
    request.setHeader(header);
    request.setPayload(payload);
    return JsonUtil.toJsonString(request);
  }

  /**
   * 更新请求体参数
   *
   * @param payload 请求体参数
   */
  private static void updatePayload(TakeAiPayloadDTO payload) {
    String takeAiParams = SystemParameter.TAKE_AI_PRAMS.getValueFromDb();
    if (StringUtils.isBlank(takeAiParams)) {
      return;
    }
    TakeAiPayloadDTO configPayload = JsonUtil.parseJson(takeAiParams, TakeAiPayloadDTO.class);
    if (configPayload == null) {
      return;
    }
    updateBasicParams(payload, configPayload);
    updateAdvancedParams(payload, configPayload);
  }

  /**
   * 更新基础参数
   */
  private static void updateBasicParams(TakeAiPayloadDTO payload, TakeAiPayloadDTO config) {
    if (StringUtils.isNotEmpty(config.getFormat())) {
      payload.setFormat(config.getFormat());
    }
    if (config.getSampleRate() != null) {
      payload.setSampleRate(config.getSampleRate());
    }
    if (config.getDelay() != null) {
      payload.setDelay(config.getDelay());
    }
    if (StringUtils.isNotEmpty(config.getHotWords())) {
      payload.setHotWords(config.getHotWords());
    }
    if (config.getMaxSegmentDurationMs() != null) {
      payload.setMaxSegmentDurationMs(config.getMaxSegmentDurationMs());
    }
  }

  /**
   * 更新高级参数
   */
  private static void updateAdvancedParams(TakeAiPayloadDTO payload, TakeAiPayloadDTO config) {
    if (config.getEnableCorrection() != null) {
      payload.setEnableCorrection(config.getEnableCorrection());
    }
    if (config.getEnableSemanticSentenceDetection() != null) {
      payload.setEnableSemanticSentenceDetection(config.getEnableSemanticSentenceDetection());
    }
    if (config.getDisfluency() != null) {
      payload.setDisfluency(config.getDisfluency());
    }
    if (config.getEnableWords() != null) {
      payload.setEnableWords(config.getEnableWords());
    }
  }

  /**
   * 发送音频数据
   *
   * @param pcmData PCM 音频数据
   * @param startTs 开始时间戳
   * @param endTs 结束时间戳
   */
  @Override
  public void sendAudio(byte[] pcmData, long startTs, long endTs) {
    if (!started || webSocket == null) {
      return;
    }

    TakeAiAudioDTO msg = new TakeAiAudioDTO();
    msg.setData(Base64.getEncoder().encodeToString(pcmData));
    msg.setStart((int) startTs);
    msg.setEnd((int) endTs);

    String jsonMsg = JsonUtil.toJsonString(msg);
    // 发送二进制帧
    webSocket.send(ByteString.of(jsonMsg.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * 停止会话
   */
  @Override
  public void stop() {
    if (!started || webSocket == null) {
      return;
    }
    String stopJson = buildStopRequest();
    webSocket.send(stopJson);
    try {
      closeLatch.await(5, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    logger.debug("TakeAi stopped, taskId={}", taskId);
    try {
      webSocket.close(1000, "done");
    }
    catch (Exception e) {
      logger.error("TakeAi Failed to close WebSocket", e);
    }
    started = false;
  }

  /**
   * 关闭会话并释放资源
   */
  @Override
  public void close() {
    stop();
    connected = false;
  }

  /**
   * 构建停止请求 JSON
   *
   * @return 停止请求的 JSON 字符串
   */
  private String buildStopRequest() {
    TakeAiHeaderDTO header = new TakeAiHeaderDTO();
    header.setAppkey(appKey);
    header.setToken(token);
    header.setNamespace(NAMESPACE_SPEECH_TRANSCRIBER);
    header.setName(NAME_STOP_TRANSCRIPTION);
    header.setMessageId(generateUuid());
    header.setTaskId(this.taskId);

    TakeAiMessageDTO request = new TakeAiMessageDTO();
    request.setHeader(header);

    return JsonUtil.toJsonString(request);
  }

  /**
   * 生成 UUID（去横杠）
   *
   * @return UUID 字符串
   */
  private String generateUuid() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * WebSocket 监听器实现
   */
  private final class TakeAiWebSocketListener extends WebSocketListener {

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
      logger.debug("TakeAi Connected");
      connected = true;
      connectLatch.countDown();
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
      logger.debug("TakeAi Received: {}", text);
      TakeAiMessageDTO message = JsonUtil.parseJson(text, TakeAiMessageDTO.class);
      if (message != null && message.getHeader() != null) {
        handleMessageByName(message);
      }
    }

    /**
     * 根据消息名称处理消息
     *
     * @param message 完整消息对象
     */
    private void handleMessageByName(TakeAiMessageDTO message) {
      TakeAiHeaderDTO header = message.getHeader();
      String name = header.getName();
      // 启动成功
      if (NAME_TRANSCRIPTION_STARTED.equals(name)) {
        logger.debug("TakeAi Transcription started");
      }
      // 处理转写结果
      else if (NAME_SENTENCE_END.equals(name) || NAME_TRANSCRIPTION_RESULT_CHANGED.equals(name) || NAME_TRANSCRIPTION_COMPLETED.equals(name)) {
        handleTranscriptionResult(name, message);
      }
      // 任务失败
      else if (NAME_TASK_FAILED.equals(name)) {
        String statusText = header.getStatusText();
        logger.error("TakeAi Task failed: {}", statusText);
        if (onError != null) {
          onError.accept(new BssException("TakeAi 任务失败: " + statusText));
        }
      }
    }

    /**
     * 处理转写结果
     *
     * @param name 消息名称
     * @param message 完整消息对象
     */
    private void handleTranscriptionResult(String name, TakeAiMessageDTO message) {
      TakeAiPayloadDTO payload = message.getPayload();
      if (payload == null) {
        logger.warn("TakeAi Payload is null for message: {}", name);
        return;
      }
      // 获取结果
      List<TakeAiResultDTO> takeAiResult = new ArrayList<>();
      if (payload.getResult() instanceof List<?>) {
        takeAiResult = JsonUtil.convert(payload.getResult(), new TypeReference<>() {
        });
      }
      if (CollectionUtils.isEmpty(takeAiResult)) {
        logger.warn("TakeAi Result list is empty for message: {}", name);
        return;
      }
      // 拼接结果
      StringBuilder sb = new StringBuilder();
      for (TakeAiResultDTO item : takeAiResult) {
        if (item != null && StringUtils.isNotEmpty(item.getText())) {
          sb.append(item.getText());
        }
      }
      String resultText = sb.toString();
      if (StringUtils.isEmpty(resultText)) {
        return;
      }
      if (onMessage != null) {
        Boolean finaled = NAME_TRANSCRIPTION_RESULT_CHANGED.equals(name) || BooleanUtils.isTrue(payload.getFinaled());
        onMessage.accept(new RealtimeAsrResultDTO(resultText, "replace", finaled));
      }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
      logger.debug("TakeAi Closing: {} {}", code, reason);
      webSocket.close(code, reason);
      closeLatch.countDown();
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
      logger.debug("TakeAi Closed: {} {}", code, reason);
      closeLatch.countDown();
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable throwable, Response response) {
      if (response == null) {
        logger.error("TakeAi Error: ", throwable);
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
        logger.error("TakeAi Error: code={}, body={}", response.code(), body, throwable);
      }
      connectError = throwable;
      connectLatch.countDown();
      closeLatch.countDown();
      if (onError != null) {
        onError.accept(throwable);
      }
    }
  }
}
