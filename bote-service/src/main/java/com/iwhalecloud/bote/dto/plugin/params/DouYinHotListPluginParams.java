package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 抖音热榜参数
 *
 * @author qian.sisheng
 * @since 2025-07-17
 */
@Getter
@Setter
@ToString
public class DouYinHotListPluginParams extends AbstractPluginParams {
  /** 榜单类型，0:抖音热榜; 2:其他榜; 默认为 0 */
  private String boardType;
  /** 榜单子类型  2：娱乐榜; 4：社会榜; hotspot_challenge：挑战榜; seeding：种草榜; 默认社会榜 */
  private String boardSubType;

  public DouYinHotListPluginParams() {
    super(PluginConsts.PLUGIN_CODE_DOUYIN_HOT_TOP);
  }
}
