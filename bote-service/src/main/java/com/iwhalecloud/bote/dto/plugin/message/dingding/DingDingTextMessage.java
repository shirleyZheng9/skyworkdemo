package com.iwhalecloud.bote.dto.plugin.message.dingding;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文本消息
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
public class DingDingTextMessage extends AbstractDingDingMessage {
  /** 文本消息 */
  private Text text;

  public DingDingTextMessage() {
    super(PluginConsts.MESSAGE_TYPE_TEXT);
  }

  @Getter
  @Setter
  @ToString
  public static class Text {
    /** 文本内容 */
    private String content;
  }
}
