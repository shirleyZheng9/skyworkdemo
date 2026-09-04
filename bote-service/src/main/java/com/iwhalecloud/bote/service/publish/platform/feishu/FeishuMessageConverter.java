package com.iwhalecloud.bote.service.publish.platform.feishu;

import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.service.publish.platform.MessageConverter;
import com.iwhalecloud.bote.service.publish.platform.feishu.dto.FeishuMessageResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 飞书消息转换器 - 参考LangBot的LarkMessageConverter设计
 *
 * @author system
 * @since 2025-01-09
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class FeishuMessageConverter extends MessageConverter {
  private static final Logger logger = LoggerFactory.getLogger(FeishuMessageConverter.class);

  @Override
  public FeishuMessageResponse standardToPlatform(StandardMessage message) {
    // 根据消息类型创建相应的响应
    String messageType = message.getMessageType();
    if (messageType == null) {
      messageType = "text";
    }
    return switch (messageType) {
      case "text" -> createTextResponse(message);
      case "rich_text" -> createRichTextResponse(message);
      case "card" -> createCardResponse(message);
      case "image" -> createImageResponse(message);
      default -> createTextResponse(message);
    };
  }

  /**
   * 创建文本消息响应
   */
  private FeishuMessageResponse.TextResponse createTextResponse(StandardMessage message) {
    FeishuMessageResponse.TextResponse response = FeishuMessageResponse.createTextResponse(message.getContent());
    response.setChatId(getChatIdFromMessage(message));
    return response;
  }

  /**
   * 创建富文本消息响应
   */
  private FeishuMessageResponse.RichTextResponse createRichTextResponse(StandardMessage message) {
    String richTextContent = buildRichTextContent(message);
    FeishuMessageResponse.RichTextResponse response = FeishuMessageResponse.createRichTextResponse(richTextContent);
    response.setChatId(getChatIdFromMessage(message));
    return response;
  }

  /**
   * 创建卡片消息响应
   */
  private FeishuMessageResponse.CardResponse createCardResponse(StandardMessage message) {
    String cardContent = buildCardContent(message);
    FeishuMessageResponse.CardResponse response = FeishuMessageResponse.createCardResponse(cardContent);
    response.setChatId(getChatIdFromMessage(message));
    return response;
  }

  /**
   * 创建图片消息响应
   */
  private FeishuMessageResponse.ImageResponse createImageResponse(StandardMessage message) {
    String imageKey = extractImageKey(message);
    FeishuMessageResponse.ImageResponse response = FeishuMessageResponse.createImageResponse(imageKey);
    response.setChatId(getChatIdFromMessage(message));
    return response;
  }

  /**
   * 构建富文本内容
   */
  private String buildRichTextContent(StandardMessage message) {
    // TODO 构建飞书富文本格式
    return message.getContent();
  }

  /**
   * 构建卡片内容
   */
  private String buildCardContent(StandardMessage message) {
    // TODO 构建飞书卡片格式
    return message.getContent();
  }

  /**
   * 从消息中提取图片Key
   */
  private String extractImageKey(StandardMessage message) {
    try {
      Object rawMessage = message.getRawMessage();
      if (rawMessage instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> messageMap = (Map<String, Object>) rawMessage;
        return (String) messageMap.get("image_key");
      }
      return message.getContent(); // 默认使用内容作为图片Key
    }
    catch (Exception e) {
      logger.warn("提取图片Key失败，使用消息内容: {}", message.getContent(), e);
      return message.getContent();
    }
  }

  /**
   * 安全获取ChatID
   */
  private String getChatIdFromMessage(StandardMessage message) {
    try {
      Object rawMessage = message.getRawMessage();
      if (rawMessage instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> messageMap = (Map<String, Object>) rawMessage;
        return (String) messageMap.get("chat_id");
      }
      return message.getGroupId();
    }
    catch (Exception e) {
      logger.warn("获取ChatID失败，使用群组ID: {}", message.getGroupId(), e);
      return message.getGroupId();
    }
  }
}
