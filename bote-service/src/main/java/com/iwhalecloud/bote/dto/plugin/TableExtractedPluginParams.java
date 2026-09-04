package com.iwhalecloud.bote.dto.plugin;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 表格提取插件参数
 *
 * @author qian.sisheng
 * @since 2025-11-24
 */
@Getter
@Setter
@ToString
public class TableExtractedPluginParams extends AbstractPluginParams {
  /** 文件路径 */
  private String file;
  /** 文件类型 */
  private String type;
  /** 文件名 type为base64时，必传 */
  private String fileName;

  public TableExtractedPluginParams() {
    super(PluginConsts.PLUGIN_CODE_EXTRACT_TABLE);
  }
}
