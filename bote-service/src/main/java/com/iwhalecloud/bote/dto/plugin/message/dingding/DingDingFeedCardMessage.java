package com.iwhalecloud.bote.dto.plugin.message.dingding;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.message.dingding.DingDingLinkMessage.Link;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * feedCard消息
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
public class DingDingFeedCardMessage extends AbstractDingDingMessage {
  /** feedCard */
  private FeedCard feedCard;
  DingDingFeedCardMessage() {
    super(PluginConsts.MESSAGE_TYPE_FEED_CARD);
  }

  @Getter
  @Setter
  @ToString
  public static class FeedCard {
    /** 链接列表 */
    private List<Link> links;
  }
}
