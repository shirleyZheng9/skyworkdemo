package com.iwhalecloud.bote.service.publish.platform.weclaw;

import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.service.publish.platform.PlatformAdapter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 个人微信（ClawBot）适配器：HTTP 长轮询收消息 + 发消息。
 *
 * @author chen.linfa
 * @since 2026-04-02
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class WeClawBotAdapter extends PlatformAdapter {
  private static final Logger logger = LoggerFactory.getLogger(WeClawBotAdapter.class);

  private final Map<String, MessageCallback> listeners = new ConcurrentHashMap<>();
  private final AtomicBoolean running = new AtomicBoolean(false);
  private volatile boolean connected;

  private final WeClawBotConfig weixinConfig;

  public WeClawBotAdapter(ResourcePublishRecordDTO publishRecord, ResourcePublishRecordMapper resourcePublishRecordMapper) {
    super(PublishChannelEnum.WECLAWBOT.getCode(), null, publishRecord);
    this.resourcePublishRecordMapper = resourcePublishRecordMapper;
    this.weixinConfig = WeClawBotConfig.fromPublishParams(publishRecord.getPublishParams());
  }

  @Override
  public Future<Boolean> sendMessage(String targetType, String targetId, StandardMessage message) {
    // 个人微信主动消息通常依赖 context_token，这里保守处理：仅支持 replyMessage 场景。
    return CompletableFuture.completedFuture(false);
  }

  @Override
  public void replyMessage(StandardMessage messageSource, StandardMessage message, boolean quoteOrigin) {
    ThreadPools.getPublish().submit(() -> replyMessageInternal(messageSource, message));
  }

  private boolean replyMessageInternal(StandardMessage messageSource, StandardMessage message) {
    touchLastAccessIfNeededForReply();
    Map<String, Object> raw = extractRawMap(messageSource);
    String toUserId = resolveReplyToUserId(messageSource, raw);
    String contextToken = resolveReplyContextToken(raw);
    if (StringUtils.isBlank(toUserId) || StringUtils.isBlank(contextToken)) {
      logger.warn("weixinpersonal reply skipped: missing toUserId/contextToken");
      return false;
    }
    if (StringUtils.isBlank(message.getContent())) {
      return true;
    }
    return sendWeClawTextReply(toUserId, message.getContent(), contextToken);
  }

  private void touchLastAccessIfNeededForReply() {
    if (publishRecord != null && publishRecord.getCallbackCode() != null) {
      updateLastAccessTime(publishRecord.getCallbackCode());
    }
  }

  @Nullable
  private String resolveReplyToUserId(StandardMessage messageSource, @Nullable Map<String, Object> raw) {
    String toUserId = messageSource.getUserId();
    if (StringUtils.isBlank(toUserId)) {
      return raw == null ? null : String.valueOf(raw.getOrDefault("from_user_id", ""));
    }
    return toUserId;
  }

  @Nullable
  private String resolveReplyContextToken(@Nullable Map<String, Object> raw) {
    return raw == null ? null : String.valueOf(raw.getOrDefault("context_token", ""));
  }

  private boolean sendWeClawTextReply(String toUserId, String text, String contextToken) {
    WeClawBotApiClient client = new WeClawBotApiClient(weixinConfig.getBotToken(), weixinConfig.getBaseUrl());
    Map<String, Object> resp = client.sendText(toUserId, text, contextToken);
    int ret = parseInt(resp.get("ret"), -1);
    int errcode = parseInt(resp.get("errcode"), 0);
    boolean ok = ret == 0 && errcode == 0;
    if (!ok) {
      logger.warn("weixinpersonal send failed: ret={}, errcode={}", ret, errcode);
    }
    return ok;
  }

  @Override
  public Future<Boolean> replyMessageChunk(StandardMessage messageSource, Object botMessage, StandardMessage message, boolean quoteOrigin, boolean isFinal) {
    // 暂不做流式：按普通文本回复即可
    replyMessage(messageSource, message, quoteOrigin);
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public void registerListener(String eventType, MessageCallback callback) {
    listeners.put(eventType, callback);
  }

  @Override
  public void unregisterListener(String eventType, @Nullable MessageCallback callback) {
    listeners.remove(eventType);
  }

  @Override
  public void runAsync() {
    ThreadPools.getPublish().submit(() -> {
      if (!weixinConfig.isValidForReceive()) {
        connected = false;
        logger.warn("weixinpersonal adapter not started: botToken is empty (please login via QR code)");
        return;
      }
      if (!running.compareAndSet(false, true)) {
        return;
      }
      connected = true;
      try {
        pollLoop();
      }
      finally {
        running.set(false);
        connected = false;
      }
    });
  }

  private void pollLoop() {
    WeClawBotApiClient client = new WeClawBotApiClient(weixinConfig.getBotToken(), weixinConfig.getBaseUrl());
    String cursor = StringUtils.defaultString(weixinConfig.getCursor());
    while (running.get()) {
      try {
        cursor = pollOnce(client, cursor);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
      catch (Exception e) {
        if (!sleepAndReconnectAfterPollFailure(e)) {
          return;
        }
      }
    }
  }

  /**
   * 单次长轮询：拉取更新、持久化游标、分发消息、必要时退避。
   *
   * @return 下一轮应使用的 cursor
   */
  private String pollOnce(WeClawBotApiClient client, String cursor) throws InterruptedException {
    Map<String, Object> resp = client.getUpdates(cursor);
    String nextCursor = applyCursorFromResponse(cursor, resp);
    List<Map<String, Object>> msgs = safeListOfMap(resp.get("msgs"));
    dispatchInboundMessages(msgs);
    backoffIfNonSuccessEmptyResponse(parseInt(resp.get("ret"), -1), msgs);
    return nextCursor;
  }

  private String applyCursorFromResponse(String cursor, Map<String, Object> resp) {
    Object newCursor = resp.get("get_updates_buf");
    if (newCursor == null) {
      return cursor;
    }
    String updated = String.valueOf(newCursor);
    weixinConfig.setCursor(updated);
    persistPublishParams();
    return updated;
  }

  private void dispatchInboundMessages(List<Map<String, Object>> msgs) {
    if (CollectionUtils.isEmpty(msgs)) {
      return;
    }
    for (Map<String, Object> msg : msgs) {
      StandardMessage standard = convertInboundMessage(msg);
      if (standard == null) {
        continue;
      }
      MessageCallback callback = listeners.get("message");
      if (callback != null) {
        callback.onMessage(standard, this);
      }
    }
  }

  /**
   * ret=-1 为长轮询超时，无消息，属于正常情况。
   */
  private void backoffIfNonSuccessEmptyResponse(int ret, List<Map<String, Object>> msgs)
    throws InterruptedException {
    if (ret == 0 || ret == -1 || CollectionUtils.isNotEmpty(msgs)) {
      return;
    }
    logger.warn("weixinpersonal getupdates non-zero ret={}, retry in 3s", ret);
    Thread.sleep(3000L);
  }

  /**
   * @return false 表示线程被中断，应结束 pollLoop
   */
  private boolean sleepAndReconnectAfterPollFailure(Exception e) {
    connected = false;
    logger.warn("weixinpersonal poll error, retry in 5s: {}", e.getMessage());
    try {
      Thread.sleep(5000L);
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return false;
    }
    connected = true;
    return true;
  }

  /**
   * 将更新后的配置（包含 cursor）回写到 publish_params 字段。
   */
  private void persistPublishParams() {
    if (publishRecord == null || StringUtils.isEmpty(publishRecord.getCallbackCode())) {
      return;
    }
    try {
      String newParamsJson = weixinConfig.toPublishParamsJson();
      resourcePublishRecordMapper.updatePublishParams(publishRecord.getCallbackCode(), newParamsJson);
    } catch (Exception e) {
      logger.warn("persist publish_params failed: callbackCode={}, error={}", publishRecord.getCallbackCode(), e.getMessage());
    }
  }

  @Nullable
  private StandardMessage convertInboundMessage(Map<String, Object> msg) {
    int msgType = parseInt(msg.get("message_type"), 0);
    // 仅处理用户→bot消息：1
    if (msgType != 1) {
      return null;
    }
    String fromUserId = String.valueOf(msg.getOrDefault("from_user_id", ""));
    String groupId = String.valueOf(msg.getOrDefault("group_id", ""));
    String contextToken = String.valueOf(msg.getOrDefault("context_token", ""));
    String text = extractText(msg);
    if (StringUtils.isBlank(text)) {
      return null;
    }
    StandardMessage standard = new StandardMessage();
    standard.setChannelType(PublishChannelEnum.WECLAWBOT.getCode());
    standard.setMessageType("text");
    standard.setTimestamp(System.currentTimeMillis());
    standard.setContent(text);
    standard.setUserId(StringUtils.defaultIfBlank(fromUserId, null));
    standard.setGroupId(StringUtils.defaultIfBlank(groupId, null));
    standard.setMessageId(StringUtils.defaultIfBlank(contextToken, null));
    standard.setRawMessage(msg);
    // 兼容你们会话逻辑：群聊优先 groupId，否则 userId
    standard.setSessionId(StringUtils.defaultIfBlank(groupId, fromUserId));
    return standard;
  }

  private String extractText(Map<String, Object> msg) {
    List<Map<String, Object>> itemList = safeListOfMap(msg.get("item_list"));
    if (CollectionUtils.isEmpty(itemList)) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (Map<String, Object> item : itemList) {
      appendTextLineFromItem(sb, item);
    }
    return sb.isEmpty() ? null : sb.toString();
  }

  private void appendTextLineFromItem(StringBuilder sb, Map<String, Object> item) {
    if (parseInt(item.get("type"), 0) != 1) {
      return;
    }
    Object textItem = item.get("text_item");
    if (!(textItem instanceof Map<?, ?> map)) {
      return;
    }
    Object t = map.get("text");
    if (t == null || StringUtils.isBlank(String.valueOf(t))) {
      return;
    }
    if (!sb.isEmpty()) {
      sb.append('\n');
    }
    sb.append(String.valueOf(t).trim());
  }

  @SuppressWarnings("unchecked")
  @Nullable
  private Map<String, Object> extractRawMap(StandardMessage messageSource) {
    Object raw = messageSource != null ? messageSource.getRawMessage() : null;
    if (raw instanceof Map) {
      return (Map<String, Object>) raw;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> safeListOfMap(Object obj) {
    if (obj instanceof List<?> list) {
      return (List<Map<String, Object>>) list;
    }
    return java.util.Collections.emptyList();
  }

  private int parseInt(Object value, int defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(String.valueOf(value));
    }
    catch (Exception ignore) {
      return defaultValue;
    }
  }

  @Override
  public void kill() {
    running.set(false);
  }

  @Override
  public boolean isStreamOutputSupported() {
    return false;
  }

  @Override
  public Future<Boolean> isMuted(String groupId) {
    return CompletableFuture.completedFuture(false);
  }

  @Override
  public Future<Boolean> createMessageCard(String messageId, StandardMessage event) {
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public boolean isConnected() {
    return connected;
  }
}
