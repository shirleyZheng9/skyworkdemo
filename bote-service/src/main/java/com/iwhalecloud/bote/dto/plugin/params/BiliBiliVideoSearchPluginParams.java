package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 哔哩哔哩视频搜索参数
 *
 * @author qian.sisheng
 * @since 2025-07-24
 */
@Setter
@Getter
@ToString
public class BiliBiliVideoSearchPluginParams extends AbstractPluginParams {
  /** cookie信息 */
  private String cookie;
  /** 页数 */
  private Integer pageNum;
  /** 每页数量 */
  private Integer pageSize;
  /** 关键字 */
  private String keyword;

  public BiliBiliVideoSearchPluginParams() {
    super(PluginConsts.PLUGIN_CODE_BILIBILI_VIDEO_SEARCH);
  }
}
