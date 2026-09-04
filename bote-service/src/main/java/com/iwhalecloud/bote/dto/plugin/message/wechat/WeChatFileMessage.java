package com.iwhalecloud.bote.dto.plugin.message.wechat;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信文件消息
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Getter
@Setter
@ToString
public class WeChatFileMessage extends AbstractWeChatMessage {
  /** 文件 */
  private File file;

  public WeChatFileMessage() {
    super(PluginConsts.MESSAGE_TYPE_FILE);
  }

  @Getter
  @Setter
  @ToString
  public static class File {
    /** 文件id */
    private String mediaId;
  }
}
