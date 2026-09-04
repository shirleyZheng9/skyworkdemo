package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书删除多维表格数据表插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-25
 */

@Getter
@Setter
@ToString
public class LarkDeleteTablePluginParams extends AbstractLarkPluginParams {

  public LarkDeleteTablePluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_DELETE_TABLE, "飞书删除多维表格数据表插件");
  }
}
