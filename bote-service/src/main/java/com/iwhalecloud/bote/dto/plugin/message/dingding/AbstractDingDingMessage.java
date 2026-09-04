package com.iwhalecloud.bote.dto.plugin.message.dingding;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;

/**
 * 钉钉消息抽象类
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */

@JsonTypeInfo(use = Id.NAME, property = "msgType", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
  @Type(name = PluginConsts.MESSAGE_TYPE_TEXT, value = DingDingTextMessage.class),
  @Type(name = PluginConsts.MESSAGE_TYPE_ACTION_CARD, value = DingDingActionCardMessage.class),
  @Type(name = PluginConsts.MESSAGE_TYPE_FEED_CARD, value = DingDingFeedCardMessage.class),
  @Type(name = PluginConsts.MESSAGE_TYPE_LINK, value = DingDingLinkMessage.class),
  @Type(name = PluginConsts.MESSAGE_TYPE_MARKDOWN, value = DingDingMarkdownMessage.class),
  @Type(name = PluginConsts.MESSAGE_TYPE_IMAGE, value = DingDingImageMessage.class),
  @Type(name = PluginConsts.MESSAGE_TYPE_VOICE, value = DingDingVoiceMessage.class),
  @Type(name = PluginConsts.MESSAGE_TYPE_FILE, value = DingDingFileMessage.class),
  @Type(name = PluginConsts.MESSAGE_TYPE_OA, value = DingDingOAMessage.class)
})
@Getter
@Setter
public abstract class AbstractDingDingMessage {
  /** 消息类型 */
  private String msgType;

  public AbstractDingDingMessage(String msgType) {
    this.msgType = msgType;
  }
}
