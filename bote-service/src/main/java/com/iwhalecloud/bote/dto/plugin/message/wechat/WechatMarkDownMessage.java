package com.iwhalecloud.bote.dto.plugin.message.wechat;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信markDown消息
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Getter
@Setter
@ToString
public class WechatMarkDownMessage extends AbstractWeChatMessage {
  /** markdown */
  private Markdown markdown;

  public WechatMarkDownMessage() {
    super(PluginConsts.MESSAGE_TYPE_MARKDOWN);
  }

  @Getter
  @Setter
  @ToString
  public static class Markdown {
    /** markdown内容，最长不超过4096个字节，必须是utf8编码 */
    private String content;
  }
}
