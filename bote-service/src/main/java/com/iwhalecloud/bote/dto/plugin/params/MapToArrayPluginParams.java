package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Map转数组插件参数
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class MapToArrayPluginParams extends AbstractPluginParams {
  /** Map对象 */
  private Object map;
  /** 数组对象中的键属性名 */
  private String leftKey;
  /** 数组对象中的值属性名 */
  private String rightKey;

  public MapToArrayPluginParams() {
    super(PluginConsts.PLUGIN_CODE_MAP_TO_ARRAY);
  }
}

