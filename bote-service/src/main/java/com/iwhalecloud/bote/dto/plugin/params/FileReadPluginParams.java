package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件读取插件
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */
@Getter
@Setter
@ToString
public class FileReadPluginParams extends AbstractPluginParams {
  /** 文件 ID */
  private Long fileId;

  public FileReadPluginParams() {
    super(PluginConsts.PLUGIN_CODE_FILE_READ);
  }
}
