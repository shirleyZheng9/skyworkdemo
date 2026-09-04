package com.iwhalecloud.bote.dto.plugin.message.wechat;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;

/**
 * 微信消息抽象类
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@JsonTypeInfo(use = Id.NAME, property = "msgType", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
    @Type(name = PluginConsts.MESSAGE_TYPE_TEXT, value = WeChatTextMessage.class),
    @Type(name = PluginConsts.MESSAGE_TYPE_IMAGE, value = WeChatImageMessage.class),
    @Type(name = PluginConsts.MESSAGE_TYPE_VOICE, value = WeChatVoiceMessage.class),
    @Type(name = PluginConsts.MESSAGE_TYPE_FILE, value = WeChatFileMessage.class),
    @Type(name = PluginConsts.MESSAGE_TYPE_NEWS, value = WeChatNewsMessage.class),
    @Type(name = PluginConsts.MESSAGE_TYPE_MARKDOWN, value = WechatMarkDownMessage.class),
    @Type(name = PluginConsts.MESSAGE_TYPE_TEMPLATE_CARD, value = WeChatTemplateCardMessage.class)
})
@Getter
@Setter
public abstract class AbstractWeChatMessage {
  /** 消息类型 */
  private String msgType;

  public AbstractWeChatMessage(String msgType) {
    this.msgType = msgType;
  }
}
