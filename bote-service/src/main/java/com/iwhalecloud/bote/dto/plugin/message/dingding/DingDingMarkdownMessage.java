package com.iwhalecloud.bote.dto.plugin.message.dingding;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * markdown消息
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
public class DingDingMarkdownMessage extends AbstractDingDingMessage {
  /** markdown */
  private Markdown markdown;

  public DingDingMarkdownMessage() {
    super(PluginConsts.MESSAGE_TYPE_MARKDOWN);
  }

  @Getter
  @Setter
  @ToString
  public static class Markdown {
    /** markdown格式的消息 */
    private String text;
    /** 首屏会话透出的展示内容 */
    private String title;
  }

}
