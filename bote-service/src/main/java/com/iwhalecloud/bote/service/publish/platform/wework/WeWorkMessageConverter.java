package com.iwhalecloud.bote.service.publish.platform.wework;

import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.service.publish.platform.MessageConverter;
import com.iwhalecloud.bote.wechat.WechatParamHelper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * 企业微信消息转换器 - 参考LangBot的WecomMessageConverter设计
 *
 * @author system
 * @since 2025-01-09
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class WeWorkMessageConverter extends MessageConverter {
  private static final Logger logger = LoggerFactory.getLogger(WeWorkMessageConverter.class);
  private final WechatParamHelper wechatParamHelper;

  public WeWorkMessageConverter() {
    this.wechatParamHelper = new WechatParamHelper();
  }

  @Override
  public WeWorkMessageResponse standardToPlatform(StandardMessage message) {
    // 根据消息类型创建相应的响应
    String messageType = message.getMessageType();
    if (messageType == null) {
      messageType = "text";
    }
    return switch (messageType) {
      case "image" -> createImageResponse(message);
      case "voice" -> createVoiceResponse(message);
      case "video" -> createVideoResponse(message);
      case "file" -> createFileResponse(message);
      default -> createTextResponse(message);
    };
  }

  /**
   * 解析企业微信消息
   * 参考LangBot的WecomMessageConverter实现
   */
  public StandardMessage parseWeWorkMessage(String encryptedMsg, String msgSignature,
                                            String timestamp, String nonce, WeWorkConfig config) {
    // 1. 解密消息
    WeWorkMessage weWorkMessage = decryptWeWorkMessage(encryptedMsg, msgSignature, timestamp, nonce, config);
    // 2. 转换为标准消息格式
    StandardMessage standardMessage = new StandardMessage();
    Assert.notNull(weWorkMessage, "企业微信消息解析失败");
    standardMessage.setMessageId(weWorkMessage.getMsgId());
    standardMessage.setUserId(weWorkMessage.getFromUserName());
    standardMessage.setUserName(weWorkMessage.getFromUserName());
    standardMessage.setMessageType(weWorkMessage.getMsgType());
    standardMessage.setContent(weWorkMessage.getContent());
    standardMessage.setTimestamp(System.currentTimeMillis());
    standardMessage.setChannelType(PublishChannelEnum.WEWORK.getCode());
    standardMessage.setRawMessage(weWorkMessage);
    return standardMessage;
  }

  /**
   * 解密企业微信消息
   */
  private WeWorkMessage decryptWeWorkMessage(String encryptedMsg, String msgSignature,
                                             String timestamp, String nonce, WeWorkConfig config) {
    if (!config.isValid()) {
      logger.error("企业微信配置不完整: token={}, aesKey={}, appId={}", config.getToken(), config.getAesKey(), config.getAppId());
      return null;
    }
    // 使用WechatParamHelper解密消息
    Map<String, String> decryptedMap = wechatParamHelper.decryptMsg(
      config.getToken(), config.getAesKey(), config.getAppId(),
      msgSignature, timestamp, nonce, encryptedMsg
    );
    return WeWorkMessage.fromMap(decryptedMap);
  }

  /**
   * 创建文本消息响应
   */
  private WeWorkMessageResponse.TextResponse createTextResponse(StandardMessage message) {
    WeWorkMessageResponse.TextResponse response = WeWorkMessageResponse.createTextResponse(message.getContent());
    response.setToUser(message.getUserId());
    response.setAgentId(getAgentIdFromMessage(message));
    return response;
  }

  /**
   * 创建图片消息响应
   */
  private WeWorkMessageResponse.ImageResponse createImageResponse(StandardMessage message) {
    // 从原始消息中提取媒体ID
    String mediaId = extractMediaId(message);
    WeWorkMessageResponse.ImageResponse response = WeWorkMessageResponse.createImageResponse(mediaId);
    response.setToUser(message.getUserId());
    response.setAgentId(getAgentIdFromMessage(message));
    return response;
  }

  /**
   * 创建语音消息响应
   */
  private WeWorkMessageResponse.VoiceResponse createVoiceResponse(StandardMessage message) {
    String mediaId = extractMediaId(message);
    WeWorkMessageResponse.VoiceResponse response = WeWorkMessageResponse.createVoiceResponse(mediaId);
    response.setToUser(message.getUserId());
    response.setAgentId(getAgentIdFromMessage(message));
    return response;
  }

  /**
   * 创建视频消息响应
   */
  private WeWorkMessageResponse.VideoResponse createVideoResponse(StandardMessage message) {
    String mediaId = extractMediaId(message);
    WeWorkMessageResponse.VideoResponse response = WeWorkMessageResponse.createVideoResponse(mediaId, "视频", "视频消息");
    response.setToUser(message.getUserId());
    response.setAgentId(getAgentIdFromMessage(message));
    return response;
  }

  /**
   * 创建文件消息响应
   */
  private WeWorkMessageResponse.FileResponse createFileResponse(StandardMessage message) {
    String mediaId = extractMediaId(message);
    WeWorkMessageResponse.FileResponse response = WeWorkMessageResponse.createFileResponse(mediaId);
    response.setToUser(message.getUserId());
    response.setAgentId(getAgentIdFromMessage(message));
    return response;
  }

  /**
   * 从消息中提取媒体ID
   */
  private String extractMediaId(StandardMessage message) {
    Object rawMessage = message.getRawMessage();
    if (rawMessage instanceof WeWorkMessage weWorkMessage) {
      // 从WeWorkMessage中提取媒体ID
      return weWorkMessage.getContent(); // 这里可能需要根据实际字段调整
    }
    else if (rawMessage instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> messageMap = (Map<String, Object>) rawMessage;
      return (String) messageMap.get("MediaId");
    }
    return message.getContent(); // 默认使用内容作为媒体ID
  }

  /**
   * 安全获取AgentID
   */
  private Long getAgentIdFromMessage(StandardMessage message) {
    Object rawMessage = message.getRawMessage();
    if (rawMessage instanceof WeWorkMessage weWorkMessage) {
      return Long.parseLong(weWorkMessage.getAgentId());
    }
    return 0L;
  }
}
