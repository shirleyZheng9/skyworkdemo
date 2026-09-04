package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书获取多维表格元数据插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-27
 */
@Setter
@Getter
@ToString
public class LarkGetBitableMetaDataPluginParams extends AbstractLarkPluginParams {

  public LarkGetBitableMetaDataPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_GET_BITABLE_META_DATA, "飞书获取多维表格元数据插件");
  }
}
