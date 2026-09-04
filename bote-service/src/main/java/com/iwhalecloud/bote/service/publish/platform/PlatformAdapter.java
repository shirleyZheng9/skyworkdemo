package com.iwhalecloud.bote.service.publish.platform;

import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.Future;
import org.springframework.lang.Nullable;

/**
 * 平台适配器基类 - 参考LangBot的MessagePlatformAdapter设计
 *
 * @author system
 * @since 2025-01-09
 */
public abstract class PlatformAdapter {

  protected String name;
  protected PublishChannelDTO config;
  protected ResourcePublishRecordDTO publishRecord;
  protected ResourcePublishRecordMapper resourcePublishRecordMapper;

  public PlatformAdapter(String name, PublishChannelDTO config, ResourcePublishRecordDTO publishRecord) {
    this.name = name;
    this.config = config;
    this.publishRecord = publishRecord;
  }

  /**
   * 发送消息
   *
   * @param targetType 目标类型：person|group
   * @param targetId 目标ID
   * @param message 消息内容
   * @return 发送结果
   */
  public abstract Future<Boolean> sendMessage(String targetType, String targetId, StandardMessage message);

  /**
   * 回复消息
   *
   * @param messageSource 消息源
   * @param message 回复消息
   * @param quoteOrigin 是否引用原消息
   */
  public abstract void replyMessage(StandardMessage messageSource, StandardMessage message, boolean quoteOrigin);

  /**
   * 流式回复消息
   *
   * @param messageSource 消息源
   * @param botMessage 机器人消息
   * @param message 回复消息
   * @param quoteOrigin 是否引用原消息
   * @param isFinal 是否结束
   * @return 回复结果
   */
  public abstract Future<Boolean> replyMessageChunk(StandardMessage messageSource, Object botMessage,
                                                               StandardMessage message, boolean quoteOrigin, boolean isFinal);

  /**
   * 注册事件监听器
   *
   * @param eventType 事件类型
   * @param callback 回调函数
   */
  public abstract void registerListener(String eventType, MessageCallback callback);

  /**
   * 注销事件监听器
   *
   * @param eventType 事件类型
   * @param callback 回调函数
   */
  public abstract void unregisterListener(String eventType, @Nullable MessageCallback callback);

  /**
   * 异步运行
   */
  public abstract void runAsync();

  /**
   * 停止适配器
   */
  public abstract void kill();

  /**
   * 是否支持流式输出
   *
   * @return 是否支持
   */
  public abstract boolean isStreamOutputSupported();

  /**
   * 是否被禁言
   *
   * @param groupId 群组ID
   * @return 是否被禁言
   */
  public abstract Future<Boolean> isMuted(String groupId);

  /**
   * 创建消息卡片
   *
   * @param messageId 消息ID
   * @param event 消息事件
   * @return 是否成功
   */
  public abstract Future<Boolean> createMessageCard(String messageId, StandardMessage event);

  public boolean verifySignature(HttpServletRequest request, String encryptedMsg) {
    throw new UnsupportedOperationException("不支持WebHook验证签名");
  }

  /**
   * 获取请求参数
   */
  protected String getParameter(Map<String, String[]> parameterMap, String name) {
    String[] values = parameterMap.get(name);
    return (values != null && values.length > 0) ? values[0] : null;
  }

  public String replyEchoStr(HttpServletRequest request) {
    throw new UnsupportedOperationException("不支持WebHook验证签名");
  }

  /**
   * 处理Webhook消息
   * 各平台适配器实现自己的消息处理逻辑
   *
   * @param request HTTP请求
   * @param encryptedMsg 加密消息
   * @param record 发布记录
   * @return 处理结果
   */
  public String handleWebhookMessage(HttpServletRequest request, String encryptedMsg, ResourcePublishRecordDTO record) {
    throw new UnsupportedOperationException("不支持Webhook消息处理");
  }

  public Map<String, String> getApplicationInfo() {
    throw new UnsupportedOperationException("不支持获取应用信息");
  }

  public String getName() {
    return name;
  }

  public ResourcePublishRecordDTO getPublishRecord() {
    return publishRecord;
  }

  public abstract boolean isConnected();
  /**
   * 更新最后访问时间
   */
  protected void updateLastAccessTime(String callbackCode) {
    if (callbackCode != null && !callbackCode.isEmpty()) {
      resourcePublishRecordMapper.updateLastAccessTime(callbackCode);
    }
  }
  /**
   * 消息回调接口 - 异步处理
   */
  @FunctionalInterface
  public interface MessageCallback {
    void onMessage(StandardMessage message, PlatformAdapter adapter);
  }
}
