package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书多维表格删除字段插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-26
 */

@Getter
@Setter
@ToString
public class LarkDeleteFieldPluginParams extends AbstractLarkPluginParams {
  /** 字段ID */
  private String fieldId;

  public LarkDeleteFieldPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_DELETE_FIELD, "飞书多维表格删除字段插件");
  }
}
