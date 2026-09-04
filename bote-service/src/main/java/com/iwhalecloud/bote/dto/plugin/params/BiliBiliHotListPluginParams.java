package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 哔哩哔哩热搜参数
 *
 * @author qian.sisheng
 * @since 2025-07-24
 */
@Setter
@Getter
@ToString
public class BiliBiliHotListPluginParams extends AbstractPluginParams {
  /** 总数 */
  private Integer total;

  public BiliBiliHotListPluginParams() {
    super(PluginConsts.PLUGIN_CODE_BILIBILI_HOT_LIST);
  }
}
