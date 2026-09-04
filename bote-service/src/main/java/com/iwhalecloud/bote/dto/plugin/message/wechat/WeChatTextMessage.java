package com.iwhalecloud.bote.dto.plugin.message.wechat;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信文本消息
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Getter
@Setter
@ToString
public class WeChatTextMessage extends AbstractWeChatMessage {
  /** 文本 */
  private Text text;

  public WeChatTextMessage() {
    super(PluginConsts.MESSAGE_TYPE_TEXT);
  }

  @Getter
  @Setter
  @ToString
  public static class Text {
    /** 文本内容 最长不超过2048个字节，必须是utf8编码 */
    private String content;
    /** userid的列表，提醒群中的指定成员(@某个成员)，@all表示提醒所有人，如果开发者获取不到userid，可以使用mentioned_mobile_list */
    private List<String> mentionedList;
    /** 手机号列表，提醒手机号对应的群成员(@某个成员)，@all表示提醒所有人 */
    private List<String> mentionedMobileList;
  }
}
