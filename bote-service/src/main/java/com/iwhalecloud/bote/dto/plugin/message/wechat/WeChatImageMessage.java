package com.iwhalecloud.bote.dto.plugin.message.wechat;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信图片消息
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Getter
@Setter
@ToString
public class WeChatImageMessage extends AbstractWeChatMessage {
  /** 图片 */
  private Image image;

  public WeChatImageMessage() {
    super(PluginConsts.MESSAGE_TYPE_IMAGE);
  }

  @Getter
  @Setter
  @ToString
  public static class Image {
    /** 图片url */
    private String picUrl;
    /** 图片mediaId */
    private String mediaId;
  }
}
