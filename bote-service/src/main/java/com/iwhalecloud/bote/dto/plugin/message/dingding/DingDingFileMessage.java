package com.iwhalecloud.bote.dto.plugin.message.dingding;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 附件消息
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */

@Getter
@Setter
@ToString
public class DingDingFileMessage extends AbstractDingDingMessage {
  /** 文件 */
  private File file;

  public DingDingFileMessage() {
    super(PluginConsts.MESSAGE_TYPE_FILE);
  }

  @Getter
  @Setter
  @ToString
  private static final class File {
    /** 文件mediaId */
    private String mediaId;
  }
}
