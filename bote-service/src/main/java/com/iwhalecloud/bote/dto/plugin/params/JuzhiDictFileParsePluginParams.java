package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 聚智平台DICT项目团队文件解析插件
 *
 * @author tingyun.wang
 * @since 2025-07-16
 */
@Getter
@Setter
@ToString
public class JuzhiDictFileParsePluginParams extends AbstractPluginParams {

  /** 文件 ID */
  private Long fileId;

  public JuzhiDictFileParsePluginParams() {
    super(PluginConsts.PLUGIN_CODE_JUZHI_DICT_FILE_PARSE);
  }

}
