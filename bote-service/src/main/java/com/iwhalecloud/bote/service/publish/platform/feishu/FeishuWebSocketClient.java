package com.iwhalecloud.bote.service.publish.platform.feishu;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.lark.oapi.core.enums.BaseUrlEnum;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.ws.Constant;
import com.lark.oapi.ws.enums.FrameType;
import com.lark.oapi.ws.enums.MessageType;
import com.lark.oapi.ws.exception.ClientException;
import com.lark.oapi.ws.exception.HeaderNotFoundException;
import com.lark.oapi.ws.exception.ServerException;
import com.lark.oapi.ws.model.ClientConfig;
import com.lark.oapi.ws.model.Endpoint;
import com.lark.oapi.ws.model.EndpointResp;
import com.lark.oapi.ws.pb.Pbbp2;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Future;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 飞书 WebSocket 客户端
 *
 * <p>基于 {@link com.lark.oapi.ws.Client} 修改, 主要改动:</p>
 * <ul>
 *   <li>支持手动关闭。官方客户端不支持手动关闭: <a href="https://github.com/larksuite/oapi-sdk-java/issues/159">issue-159</a></li>
 *   <li>pingLoop 避免吞掉 InterruptedException, 以避免无法关闭</li>
 *   <li>共用一个线程池，不再为每个连接单独创建线程池</li>
 *   <li>共用 OkHttpClient, 不再为每个连接单独创建 OkHttpClient 实例</li>
 *   <li>优化日志</li>
 * </ul>
 *
 * @author bianjp
 * @since 2026-03-19
 */
@SuppressWarnings("PMD.GuardLogStatement")
@SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
public class FeishuWebSocketClient implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(FeishuWebSocketClient.class);
  /** 最大重连次数 */
  private static final int MAX_RECONNECT_COUNT = 10000;

  /** 应用 ID */
  private final String appId;
  /** 应用密钥 */
  private final String appSecret;
  /** 事件处理器 */
  private final EventDispatcher eventHandler;

  /** WebSocket 连接 */
  private WebSocket socket;
  /** WebSocket 连接 URL */
  private String connUrl;
  /** 服务 ID */
  private String serviceId;
  /** 连接 ID */
  private String connId;
  /** 首次重连d随机抖动最大时间(秒) */
  private int reconnectNonce = 30;
  /** 最大重连次数，0 表示不重连 */
  private int reconnectCount = MAX_RECONNECT_COUNT;
  /** 重连间隔时间(秒) */
  private long reconnectInterval = 120;
  /** 心跳间隔时间(秒) */
  private long pingInterval = 60;
  /** 消息数据缓存 */
  private final Cache<String, byte[][]> cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build();

  /** 是否正在重连 */
  private volatile boolean isReconnecting;
  /** 是否已关闭。关闭后不再重连 */
  private volatile boolean closed;
  /** 心跳任务 */
  private Future<?> pingFuture;

  public FeishuWebSocketClient(String appId, String appSecret, EventDispatcher eventHandler) {
    this.appId = appId;
    this.appSecret = appSecret;
    this.eventHandler = eventHandler;
  }

  /**
   * 启动
   */
  public void start() {
    try {
      connect();
    }
    catch (ClientException e) {
      logger.error("Failed to start websocket: appId={}", appId, e);
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to start websocket: appId={}", appId, e);
      disconnect();
      reconnect();
    }
    // 启动心跳任务
    pingFuture = ThreadPools.getCommon().submit(this::pingLoop);
  }

  @Override
  public void close() {
    closed = true;
    if (pingFuture != null) {
      pingFuture.cancel(true);
    }
    if (socket != null) {
      socket.close(1000, "client closed");
    }
  }

  /**
   * 心跳循环
   */
  @SuppressWarnings({"BusyWait", "PMD.UnusedPrivateMethod"})
  private void pingLoop() {
    try {
      Thread.sleep(2000);
      while (!closed) {
        try {
          if (socket != null) {
            Pbbp2.Frame frame = newPingFrame(Integer.parseInt(serviceId));
            socket.send(ByteString.of(frame.toByteArray()));
            logger.trace("websocket ping success: appId={}, connId={}", appId, connId);
          }
        }
        catch (Exception e) {
          logger.warn("websocket ping failed: appId={}, connId={}", appId, connId, e);
        }
        finally {
          Thread.sleep(pingInterval * 1000);
        }
      }
    }
    catch (InterruptedException e) {
      logger.trace("websocket ping task interrupted: appId={}, connId={}", appId, connId);
    }
  }

  /**
   * 断开连接
   */
  private synchronized void disconnect() {
    if (socket == null) {
      return;
    }
    try {
      socket.close(1000, "client closed");
      logger.debug("websocket disconnected: appId={}, connId={}, url={}", appId, connId, connUrl);
    }
    finally {
      socket = null;
      connUrl = null;
      connId = null;
      serviceId = null;
    }
  }

  /**
   * 重连
   */
  @SuppressWarnings("BusyWait")
  private void reconnect() {
    // 已关闭时不再重连
    if (closed) {
      return;
    }
    isReconnecting = true;
    try {
      logger.debug("websocket reconnecting start: appId={}", appId);
      // 首次重连随机抖动
      if (reconnectNonce > 0) {
        Thread.sleep(RandomUtils.insecure().randomInt(0, reconnectNonce * 1000));
      }
      for (int i = 0; i < reconnectCount; i++) {
        // 已关闭时停止重试
        if (closed || socket != null) {
          return;
        }
        if (tryConnect(i)) {
          return;
        }
        Thread.sleep(reconnectInterval * 1000);
      }
      throw new BssException("无法连接飞书 WebSocket 服务器，已重试 " + reconnectCount + " 次");
    }
    catch (InterruptedException e) {
      logger.debug("websocket reconnect loop interrupted: appId={}", appId);
      Thread.currentThread().interrupt();
    }
    finally {
      isReconnecting = false;
    }
  }

  /**
   * 尝试重连
   */
  private boolean tryConnect(int retryCount) {
    retryCount++;
    logger.debug("websocket reconnecting start: appId={}, count={}", appId, retryCount);
    try {
      connect();
      return true;
    }
    catch (ClientException e) {
      logger.error("websocket reconnect failed: appId={}, error={}", appId, e.toString());
      throw e;
    }
    catch (Exception e) {
      logger.error("websocket reconnect failed: appId={}", appId, e);
      return false;
    }
  }

  /**
   * 获取 WebSocket 连接地址
   */
  private String getWebSocketUrl() throws IOException {
    String body = String.format("{\"AppID\": \"%s\", \"AppSecret\": \"%s\"}", appId, appSecret);
    //noinspection UastIncorrectHttpHeaderInspection
    Request request = new Request.Builder()
      .url(BaseUrlEnum.FeiShu.getUrl() + Constant.GEN_ENDPOINT_URI)
      .header("locale", "zh")
      .post(RequestBody.create(body, MediaType.parse("application/json; charset=utf-8")))
      .build();
    try (Response response = ModelHttpClient.getClient().newCall(request).execute()) {
      String json = ModelHttpClient.readResponseBody(response);
      if (response.code() != 200) {
        logger.error("Failed to get websocket url: appId={}, status={}, body={}", appId, response.code(), json);
        throw new BssException("获取飞书 WebSocket 连接地址失败: status=" + response.code());
      }
      if (StringUtils.isEmpty(json)) {
        throw new BssException("获取飞书 WebSocket 连接地址失败，响应为空");
      }
      EndpointResp resp = Jsons.DEFAULT.fromJson(json, EndpointResp.class);
      if (resp.getCode() != Constant.OK) {
        logger.error("Failed to get websocket url: appId={}, response={}", appId, json);
        if (resp.getCode() == Constant.SYSTEM_BUSY) {
          throw new BssException("获取飞书 WebSocket 连接地址失败: 系统繁忙");
        }
        else if (resp.getCode() == Constant.INTERNAL_ERROR) {
          throw new BssException("获取飞书 WebSocket 连接地址失败: " + resp.getMsg());
        }
        else {
          throw new ClientException(resp.getCode(), resp.getMsg());
        }
      }

      Endpoint data = resp.getData();
      if (data.getClientConfig() != null) {
        configure(data.getClientConfig());
      }

      return data.getUrl();
    }
  }

  /**
   * 解析 WebSocket 地址
   */
  private HttpUrl parseWebSocketUrl(String url) {
    HttpUrl httpUrl = HttpUrl.parse(url.replace("wss://", "https://").replace("ws://", "https://"));
    if (httpUrl == null) {
      throw new ServerException(500, "connect url is invalid");
    }
    return httpUrl;
  }

  /**
   * 连接
   */
  private synchronized void connect() throws IOException {
    if (closed || socket != null) {
      return;
    }
    connUrl = getWebSocketUrl();
    HttpUrl httpUrl = parseWebSocketUrl(connUrl);
    connId = httpUrl.queryParameter(Constant.DEVICE_ID);
    serviceId = httpUrl.queryParameter(Constant.SERVICE_ID);

    Request request = new Request.Builder().url(connUrl).build();
    socket = ModelHttpClient.getClient().newWebSocket(request, new WebSocketListener() {
      @Override
      public void onOpen(WebSocket webSocket, Response response) {
        logger.debug("websocket connection opened: appId={}, url={}", appId, connUrl);
      }

      @Override
      public void onMessage(WebSocket webSocket, ByteString bytes) {
        ThreadPools.getCommon().submit(() -> handleMessage(bytes));
      }

      @Override
      public void onClosed(WebSocket webSocket, int code, String reason) {
        disconnect();
        reconnect();
      }

      @Override
      @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
      public void onFailure(WebSocket webSocket, Throwable throwable, @Nullable Response response) {
        // 是否可重试，某些客户端错误不重试
        boolean retryable = true;
        try {
          retryable = handleConnectionFailure(throwable, response);
        }
        catch (Exception e) {
          logger.error("Failed to process websocket failure: appId={}", appId, e);
        }

        // 自动重连
        if (retryable && !isReconnecting) {
          disconnect();
          reconnect();
        }
      }
    });
  }

  /**
   * 处理连接失败
   *
   * @return 是否可重试
   */
  private boolean handleConnectionFailure(Throwable throwable, @Nullable Response response) {
    if (response == null) {
      logger.error("websocket connection failed: appId={}, url={}", appId, connUrl, throwable);
      return true;
    }

    String code = response.header(Constant.HEADER_HANDSHAKE_STATUS);
    String msg = response.header(Constant.HEADER_HANDSHAKE_MSG);
    if (StringUtils.isEmpty(code) || StringUtils.isEmpty(msg)) {
      logger.error("websocket connection failed: appId={}, url={}, status={}, code={}, msg={}", appId, connUrl, response.code(), code, msg, throwable);
      return true;
    }

    boolean retryable = true;
    int c = Integer.parseInt(code);
    if (c == Constant.AUTH_FAILED) {
      String authCode = response.header(Constant.HEADER_HANDSHAKE_AUTH_ERRCODE);
      if (String.valueOf(Constant.EXCEED_CONN_LIMIT).equals(authCode)) {
        retryable = false;
        logger.error("websocket connection exceed connection limit: appId={}, url={}", appId, connUrl);
      }
      else {
        logger.error("websocket connection auth failed: appId={}, url={}, code={}, msg={}", appId, connUrl, code, msg);
      }
    }
    else if (c == Constant.FORBIDDEN) {
      retryable = false;
      logger.error("websocket connection forbidden: appId={}, url={}, code={}, msg={}", appId, connUrl, code, msg);
    }
    else {
      logger.error("websocket connection failed: appId={}, url={}, code={}, msg={}", appId, connUrl, code, msg);
    }

    return retryable;
  }

  /**
   * 处理消息
   */
  private void handleMessage(ByteString bytes) {
    try {
      byte[] msg = bytes.toByteArray();
      Pbbp2.Frame frame = Pbbp2.Frame.parseFrom(msg);
      FrameType ft = FrameType.of(frame.getMethod());

      switch (ft) {
        case CONTROL:
          handleControlFrame(frame);
          break;
        case DATA:
          handleDataFrame(frame);
          break;
        default:
          logger.warn("unknown frame type: appId={}, type={}", appId, ft);
          break;
      }
    }
    catch (Exception e) {
      logger.error("Failed to handle message: appId={}, connId={}", appId, connId, e);
    }
  }

  /**
   * 处理控制帧
   */
  private void handleControlFrame(Pbbp2.Frame frame) {
    List<Pbbp2.Header> hs = frame.getHeadersList();
    MessageType mt = MessageType.of(getString(hs, Constant.HEADER_TYPE));

    switch (mt) {
      case PING:
        return;
      case PONG:
        logger.trace("Received pong: appId={}, connId={}", appId, connId);
        if (!frame.hasPayload()) {
          return;
        }
        ClientConfig conf = Jsons.DEFAULT.fromJson(frame.getPayload().toStringUtf8(), ClientConfig.class);
        configure(conf);
        break;
      default:
        // 忽略不关注的事件
        break;
    }
  }

  /**
   * 处理数据帧
   */
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  private void handleDataFrame(Pbbp2.Frame frame) {
    List<Pbbp2.Header> headers = frame.getHeadersList();
    String msgId = getString(headers, Constant.HEADER_MESSAGE_ID);
    String traceId = getString(headers, Constant.HEADER_TRACE_ID);
    int sum = getInteger(headers, Constant.HEADER_SUM);
    int seq = getInteger(headers, Constant.HEADER_SEQ);
    String type = getString(headers, Constant.HEADER_TYPE);

    byte[] pl = frame.getPayload().toByteArray();
    if (sum > 1) {
      // 合包
      pl = combine(msgId, sum, seq, pl);
      if (pl == null) {
        return;
      }
    }

    MessageType messageType = MessageType.of(type);
    logger.trace("Received message: appId={}, connId={}, messageType={}, messageId={}, traceId={}, payload={}",
      appId, connId, messageType.getName(), msgId, traceId, new String(pl, StandardCharsets.UTF_8));

    com.lark.oapi.ws.model.Response response = new com.lark.oapi.ws.model.Response(200);
    long start = System.currentTimeMillis();
    try {
      switch (messageType) {
        case EVENT:
          Object data = eventHandler.doWithoutValidation(pl);
          if (data != null) {
            response.setData(Jsons.DEFAULT.toJson(data).getBytes(StandardCharsets.UTF_8));
          }
          break;
        case CARD:
          return;
        default:
          // 忽略不关注的消息类型
          break;
      }
    }
    catch (Throwable e) {
      logger.error("Failed to handle message: appId={}, connId={}, messageType={}, messageId={}, traceId={}",
        appId, connId, messageType.getName(), msgId, traceId, e);
      response = new com.lark.oapi.ws.model.Response(500);
    }
    long end = System.currentTimeMillis();

    byte[] resp = Jsons.DEFAULT.toJson(response).getBytes(StandardCharsets.UTF_8);
    byte[] bytes = frame.toBuilder()
      .setPayload(com.lark.oapi.google.protobuf.ByteString.copyFrom(resp))
      .addHeaders(Pbbp2.Header.newBuilder()
        .setKey(Constant.HEADER_BIZ_RT)
        .setValue(String.valueOf(end - start))
        .build())
      .build()
      .toByteArray();
    socket.send(ByteString.of(bytes));
  }

  /**
   * 合包
   */
  @Nullable
  private byte[] combine(String msgId, int sum, int seq, byte[] bytes) {
    byte[][] val = cache.getIfPresent(msgId);
    if (val == null) {
      byte[][] buf = new byte[sum][];
      buf[seq] = bytes;
      cache.put(msgId, buf);
      return null;
    }

    val[seq] = bytes;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    for (byte[] v : val) {
      if (v == null) {
        cache.put(msgId, val);
        return null;
      }
      outputStream.writeBytes(v);
    }

    return outputStream.toByteArray();
  }

  /**
   * 更新配置
   */
  private void configure(ClientConfig conf) {
    if (conf.getReconnectCount() != null && conf.getReconnectCount() >= 0 && conf.getReconnectCount() <= MAX_RECONNECT_COUNT) {
      reconnectCount = conf.getReconnectCount();
    }
    else {
      reconnectCount = MAX_RECONNECT_COUNT;
    }
    reconnectInterval = conf.getReconnectInterval();
    reconnectNonce = conf.getReconnectNonce();
    pingInterval = conf.getPingInterval();
  }

  private String getString(List<Pbbp2.Header> headers, String key) {
    return headers.stream()
      .filter(o -> o.getKey().equals(key))
      .findFirst()
      .map(Pbbp2.Header::getValue)
      .orElseThrow(() -> new HeaderNotFoundException(key));
  }

  private int getInteger(List<Pbbp2.Header> headers, String key) {
    return headers.stream()
      .filter(o -> o.getKey().equals(key))
      .findFirst()
      .map(Pbbp2.Header::getValue)
      .map(Integer::parseInt)
      .orElseThrow(() -> new HeaderNotFoundException(key));
  }

  private Pbbp2.Frame newPingFrame(int serviceId) {
    return Pbbp2.Frame.newBuilder()
      .setService(serviceId)
      .setMethod(FrameType.CONTROL.getCode())
      .addHeaders(Pbbp2.Header.newBuilder()
        .setKey(Constant.HEADER_TYPE)
        .setValue(MessageType.PING.getName())
        .build())
      .setSeqID(0)
      .setLogID(0)
      .build();
  }
}
