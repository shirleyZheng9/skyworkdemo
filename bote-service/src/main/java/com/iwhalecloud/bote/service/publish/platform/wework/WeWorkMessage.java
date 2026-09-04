package com.iwhalecloud.bote.service.publish.platform.wework;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 企业微信消息类
 * 替代Map<String, String>，提供类型安全
 *
 * @author system
 * @since 2025-01-09
 */
@Setter
@Getter
@JacksonXmlRootElement(localName = "xml")
public class WeWorkMessage {
  @JacksonXmlProperty(localName = "ToUserName")
  private String toUserName;
  @JacksonXmlProperty(localName = "FromUserName")
  private String fromUserName;
  @JacksonXmlProperty(localName = "CreateTime")
  private String createTime;
  @JacksonXmlProperty(localName = "MsgType")
  private String msgType;
  @JacksonXmlProperty(localName = "Content")
  private String content;
  @JacksonXmlProperty(localName = "MsgId")
  private String msgId;
  @JacksonXmlProperty(localName = "AgentID")
  private String agentId;
  private String chatId;

  /**
   * 从Map创建WeWorkMessage
   */
  public static WeWorkMessage fromMap(Map<String, String> messageMap) {
    WeWorkMessage message = new WeWorkMessage();
    if (messageMap != null) {
      message.setMsgId(messageMap.get("MsgId"));
      message.setFromUserName(messageMap.get("FromUserName"));
      message.setToUserName(messageMap.get("ToUserName"));
      message.setMsgType(messageMap.get("MsgType"));
      message.setContent(messageMap.get("Content"));
      message.setCreateTime(messageMap.get("CreateTime"));
      message.setAgentId(messageMap.get("AgentID"));
      message.setChatId(messageMap.get("ChatId"));
    }
    return message;
  }

  /**
   * 转换为Map（用于向后兼容）
   */
  public Map<String, String> toMap() {
    Map<String, String> map = new HashMap<>();
    map.put("MsgId", msgId);
    map.put("FromUserName", fromUserName);
    map.put("ToUserName", toUserName);
    map.put("MsgType", msgType);
    map.put("Content", content);
    map.put("CreateTime", createTime);
    map.put("AgentID", agentId);
    map.put("ChatId", chatId);
    return map;
  }
}
