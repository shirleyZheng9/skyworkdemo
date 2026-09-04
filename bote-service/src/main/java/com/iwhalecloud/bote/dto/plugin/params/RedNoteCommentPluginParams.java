package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;

/**
 * 小红书笔记评论插件参数
 *
 * @author qian.sisheng
 * @since 2025-07-25
 */
@Getter
@Setter
public class RedNoteCommentPluginParams extends AbstractPluginParams {
  /** 登录cookie */
  private String cookie;
  /** 笔记链接 */
  private String url;

  public RedNoteCommentPluginParams() {
    super(PluginConsts.PLUGIN_CODE_RED_NOTE_COMMENT);
  }
}
