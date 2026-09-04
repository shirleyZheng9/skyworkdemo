package com.iwhalecloud.bote.dto.plugin.message.dingding;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片消息
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */

@Getter
@Setter
@ToString
public class DingDingImageMessage extends AbstractDingDingMessage {
  /** 图片 */
  private Image image;
  public DingDingImageMessage() {
    super(PluginConsts.MESSAGE_TYPE_IMAGE);
  }

  @Getter
  @Setter
  @ToString
  private static final class Image {
    /** 图片的media_id */
    private String mediaId;
  }
}
