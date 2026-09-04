package com.iwhalecloud.bote.service.publish.platform.dingtalk;

import com.iwhalecloud.bote.dto.publish.StandardMessage;
import com.iwhalecloud.bote.service.publish.platform.MessageConverter;
import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉消息转换器 - 参考LangBot的DingTalkMessageConverter设计
 *
 * @author system
 * @since 2025-01-09
 */
public class DingTalkMessageConverter extends MessageConverter {
  @Override
  public Map<String, Object> standardToPlatform(StandardMessage message) {
    Map<String, Object> platformMessage = new HashMap<>();
    // 设置消息类型
    String messageType = message.getMessageType();
    if (messageType == null) {
      messageType = "text";
    }
    platformMessage.put("msgtype", messageType);
    // 根据消息类型设置内容
    switch (messageType.toLowerCase()) {
      case "text":
        Map<String, Object> text = new HashMap<>();
        text.put("content", message.getContent() != null ? message.getContent() : "");
        platformMessage.put("text", text);
        break;
      case "markdown":
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", "消息");
        markdown.put("text", message.getContent() != null ? message.getContent() : "");
        platformMessage.put("markdown", markdown);
        break;
      case "action_card":
        Map<String, Object> actionCard = new HashMap<>();
        actionCard.put("title", "消息");
        actionCard.put("markdown", message.getContent() != null ? message.getContent() : "");
        actionCard.put("single_title", "查看详情");
        actionCard.put("single_url", "https://www.dingtalk.com");
        platformMessage.put("action_card", actionCard);
        break;
      default:
        // 默认使用文本消息
        Map<String, Object> defaultText = new HashMap<>();
        defaultText.put("content", message.getContent() != null ? message.getContent() : "");
        platformMessage.put("text", defaultText);
        platformMessage.put("msgtype", "text");
        break;
    }
    // 设置时间戳
    if (message.getTimestamp() != null) {
      platformMessage.put("timestamp", message.getTimestamp());
    }
    return platformMessage;
  }
}
