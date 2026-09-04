package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书创建多维表格插件参数
 *
 * @author auto
 * @since 2025-07-18
 */
@Getter
@Setter
@ToString
public class LarkCreateBitablePluginParams extends AbstractLarkPluginParams {

  /** 多维表格名称，用于createBitable操作 */
  private String name;
  /** 文件夹token，用于createBitable操作，可选 */
  private String folderToken;

  public LarkCreateBitablePluginParams() {
    super(PluginConsts.PLUGIN_CODE_CREATE_LARK_BITABLE, "飞书创建多维表格插件");
  }
}
