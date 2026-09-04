package com.iwhalecloud.bote.service.publish.platform.wework;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.service.publish.platform.PlatformAdapter;
import com.iwhalecloud.bote.wechat.WechatParamHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 企业微信适配器 - 参考LangBot的WecomAdapter设计
 *
 * @author system
 * @since 2025-01-09
 */
public final class WeWorkAdapter extends PlatformAdapter {
  private static final Logger logger = LoggerFactory.getLogger(WeWorkAdapter.class);
  private final WeWorkMessageConverter messageConverter;
  private final WeWorkApiClient apiClient;
  private final WechatParamHelper wechatParamHelper;
  private static final XmlMapper xmlMapper = new XmlMapper();
  private final Map<String, MessageCallback> listeners = new ConcurrentHashMap<>();
  private String accessToken;
  private final WeWorkConfig weWorkConfig;

  public WeWorkAdapter(PublishChannelDTO config, ResourcePublishRecordDTO publishRecord, WechatParamHelper wechatParamHelper, ResourcePublishRecordMapper resourcePublishRecordMapper) {
    super(PublishChannelEnum.WEWORK.getCode(), config, publishRecord);
    this.wechatParamHelper = wechatParamHelper;
    this.resourcePublishRecordMapper = resourcePublishRecordMapper;
    this.messageConverter = new WeWorkMessageConverter();
    this.apiClient = new WeWorkApiClient();
    this.weWorkConfig = WeWorkConfig.fromMap(config);
    // 验证配置
    if (!weWorkConfig.isValid()) {
      throw new BssException("企业微信配置不完整");
    }
  }

  @Override
  public Future<Boolean> sendMessage(String targetType, String targetId, StandardMessage message) {
    return ThreadPools.getPublish().submit(() -> {
      // 更新最后访问时间
      if (publishRecord != null && publishRecord.getCallbackCode() != null) {
        updateLastAccessTime(publishRecord.getCallbackCode());
      }
      WeWorkMessageResponse platformMessage = messageConverter.standardToPlatform(message);
      // 解析targetId: user_id|agent_id
      String[] parts = targetId.split("\\|");
      String userId = parts[0];
      Long agentId = Long.parseLong(parts[1]);
      if ("person".equals(targetType)) {
        return sendPrivateMessage(userId, agentId, platformMessage);
      }
      logger.warn("企业微信暂不支持群组消息发送");
      return false;
    });
  }

  @Override
  public void replyMessage(StandardMessage messageSource, StandardMessage message, boolean quoteOrigin) {
    ThreadPools.getPublish().submit(() -> {
      // 更新最后访问时间
      if (publishRecord != null && publishRecord.getCallbackCode() != null) {
        updateLastAccessTime(publishRecord.getCallbackCode());
      }
      WeWorkMessageResponse platformMessage = messageConverter.standardToPlatform(message);
      // 从消息源获取用户信息
      String userId = messageSource.getUserId();
      Long agentId = getAgentIdFromMessage(messageSource);
      return sendPrivateMessage(userId, agentId, platformMessage);
    });
  }

  @Override
  public Future<Boolean> replyMessageChunk(StandardMessage messageSource, Object botMessage, StandardMessage message, boolean quoteOrigin, boolean isFinal) {
    // 企业微信暂不支持流式输出
    return CompletableFuture.completedFuture(false);
  }

  @Override
  public void registerListener(String eventType, MessageCallback callback) {
    listeners.put(eventType, callback);
    logger.info("注册企业微信事件监听器: {}", eventType);
  }

  @Override
  public void unregisterListener(String eventType, @Nullable MessageCallback callback) {
    listeners.remove(eventType);
    logger.info("注销企业微信事件监听器: {}", eventType);
  }

  @Override
  public void runAsync() {
    // 企业微信使用Webhook模式，不需要启动长连接
  }

  @Override
  public void kill() {
    ThreadPools.getPublish().submit(() -> {
      // 停止企业微信连接
      stopWeWorkConnection();
      return true;
    });
  }

  @Override
  public boolean isStreamOutputSupported() {
    return false; // 企业微信暂不支持流式输出
  }

  @Override
  public Future<Boolean> isMuted(String groupId) {
    return CompletableFuture.completedFuture(false); // 企业微信暂不支持群组禁言检查
  }

  /**
   * 从XML中提取加密内容
   */
  private String extractEncryptFromXml(String xml) {
    try {
      // 使用XmlMapper解析XML
      WeWorkEncryptedMessage message = xmlMapper.readValue(xml, WeWorkEncryptedMessage.class);
      if (message == null || message.getEncrypt() == null) {
        logger.error("XML解析失败或未找到加密内容");
        return null;
      }
      //      // 验证ToUserName是否匹配corpid
//      if (config.get("token").equals(message.getToUserName())) {
//        log.warn("ToUserName不匹配: 期望={}, 实际={}", corpid, message.getToUserName());
//      }
      return message.getEncrypt();
    }
    catch (Exception e) {
      logger.error("解析XML失败", e);
      return null;
    }
  }

  @Override
  public Future<Boolean> createMessageCard(String messageId, StandardMessage event) {
    return CompletableFuture.completedFuture(false); // 企业微信暂不支持卡片消息
  }

  public boolean verifySignature(HttpServletRequest request, String encryptedMsg) {
    Map<String, String[]> parameterMap = request.getParameterMap();
    String signature = getParameter(parameterMap, "msg_signature");
    String timestamp = getParameter(parameterMap, "timestamp");
    String nonce = getParameter(parameterMap, "nonce");
    String echostr = getParameter(parameterMap, "echostr");
    if (encryptedMsg != null) {
      echostr = extractEncryptFromXml(encryptedMsg);
    }
    return signature.equals(wechatParamHelper.getSHA1(config.getToken(), timestamp, nonce, echostr));
  }

  public String replyEchoStr(HttpServletRequest request) {
    Map<String, String[]> parameterMap = request.getParameterMap();
    String echostr = getParameter(parameterMap, "echostr");
    try {
      return wechatParamHelper.decryptInternal(echostr, Base64.decodeBase64(config.getAesKey() + "="), config.getAppId());
    }
    catch (Exception e) {
      throw new BssException("echostr解析失败", e);
    }
  }

  /**
   * 发送私聊消息
   */
  private boolean sendPrivateMessage(String userId, Long agentId, WeWorkMessageResponse messageResponse) {
    // 获取access_token
    if (accessToken == null) {
      accessToken = apiClient.getAccessToken(weWorkConfig.getAppId(), weWorkConfig.getSecret());
    }
    // 根据消息类型发送不同的消息
    switch (messageResponse) {
      case WeWorkMessageResponse.TextResponse textResponse -> {
        return apiClient.sendTextMessage(accessToken, userId, agentId, textResponse.getText().getContent());
      }
      case WeWorkMessageResponse.ImageResponse imageResponse -> {
        return apiClient.sendImageMessage(accessToken, userId, agentId, imageResponse.getImage().getMediaId());
      }
      case null, default -> {
        return false;
      }
    }
  }

  /**
   * 停止企业微信连接
   */
  private void stopWeWorkConnection() {
    // 企业微信使用Webhook模式，不需要停止长连接
    // 只需要清理相关资源
    accessToken = null;
  }

  @Override
  public String handleWebhookMessage(HttpServletRequest request, String encryptedMsg, ResourcePublishRecordDTO record) {
    // 1. 验证消息签名
    boolean isValid = verifySignature(request, encryptedMsg);
    if (!isValid) {
      logger.error("企业微信消息签名验证失败");
      throw new BssException("企业微信消息签名验证失败");
    }
    // 2. 解析企业微信消息
    StandardMessage message = parseWeWorkMessage(request, encryptedMsg);
    if (message == null) {
      logger.warn("企业微信消息解析失败");
      return "success";
    }
    // 3. 触发消息监听器（由SdkConnectionManager注册的智能体处理）
    MessageCallback callback = listeners.get("message");
    if (callback != null) {
      callback.onMessage(message, this);
    }
    return "success";
  }

  /**
   * 解析企业微信消息
   */
  private StandardMessage parseWeWorkMessage(HttpServletRequest request, String encryptedMsg) {
    // 从请求参数中获取基本信息
    String msgSignature = request.getParameter("msg_signature");
    String timestamp = request.getParameter("timestamp");
    String nonce = request.getParameter("nonce");
    if (encryptedMsg == null || encryptedMsg.isEmpty()) {
      logger.warn("企业微信消息内容为空");
      return null;
    }
    // 使用WeWorkMessageConverter解析消息
    return messageConverter.parseWeWorkMessage(encryptedMsg, msgSignature, timestamp, nonce, weWorkConfig);
  }

  /**
   * 安全获取AgentID
   */
  private Long getAgentIdFromMessage(StandardMessage message) {
    Object rawMessage = message.getRawMessage();
    if (rawMessage instanceof WeWorkMessage weWorkMessage) {
      return Long.parseLong(weWorkMessage.getAgentId());
    }
    else if (rawMessage instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, String> messageMap = (Map<String, String>) rawMessage;
      String agentIdStr = messageMap.get("AgentID");
      return agentIdStr != null ? Long.parseLong(agentIdStr) : Long.parseLong(weWorkConfig.getAppId());
    }
    // 默认使用配置中的AppId
    return Long.parseLong(weWorkConfig.getAppId());
  }

  /**
   * 通过调用gettoken接口检查企业微信连接状态
   */
  public boolean isConnected() {
    // 调用gettoken接口测试连接
    String testToken = apiClient.getAccessToken(weWorkConfig.getAppId(), weWorkConfig.getSecret());
    return testToken != null && !testToken.isEmpty();
  }

  /**
   * 企业微信加密消息XML结构
   */
  @Setter
  @Getter
  @JacksonXmlRootElement(localName = "xml")
  public static class WeWorkEncryptedMessage {
    @JacksonXmlProperty(localName = "ToUserName")
    private String toUserName;
    @JacksonXmlProperty(localName = "Encrypt")
    private String encrypt;
    @JacksonXmlProperty(localName = "AgentID")
    private String agentId;
  }
}
