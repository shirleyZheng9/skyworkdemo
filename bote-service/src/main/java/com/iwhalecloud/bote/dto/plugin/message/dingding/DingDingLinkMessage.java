package com.iwhalecloud.bote.dto.plugin.message.dingding;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 链接消息
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */

@Getter
@Setter
@ToString
public class DingDingLinkMessage extends AbstractDingDingMessage {
  /** 链接 */
  private Link link;

  public DingDingLinkMessage() {
    super(PluginConsts.MESSAGE_TYPE_LINK);
  }

  @Getter
  @Setter
  @ToString
  public static class Link {
    /** 点击消息跳转的URL，打开方式如下： 移动端，在钉钉客户端内打开 PC端 */
    private String messageUrl;
    /** 图片URL */
    private String picUrl;
    /** 消息内容 */
    private String text;
    /** 消息标题 */
    private String title;
  }
}
