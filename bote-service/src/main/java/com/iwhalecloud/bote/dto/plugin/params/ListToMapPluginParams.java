package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数组转Map插件参数
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class ListToMapPluginParams extends AbstractPluginParams {
  /** 对象列表 */
  private List<Object> list;
  /** Map的键字段名 */
  private String leftKey;
  /** Map的值字段名（可选，为空时整个对象作为值） */
  private String rightKey;

  public ListToMapPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LIST_TO_MAP);
  }
}
