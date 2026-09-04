package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 抖音视频评论插件参数
 *
 * @author qian.sisheng
 * @since 2025-07-18
 */
@Setter
@Getter
@ToString
public class DouYinCommentPluginParams extends AbstractPluginParams {
  /** 视频分享链接，如 */
  private String url;
  /** 分页页数，默认为0 */
  private String pageNum;
  /** 分页大小，默认为20 */
  private String pageSize;
  /** 账号 */
  private String userName;
  /** 密码 */
  private String password;

  public DouYinCommentPluginParams() {
    super(PluginConsts.PLUGIN_CODE_DOUYIN_COMMENT);
  }
}
