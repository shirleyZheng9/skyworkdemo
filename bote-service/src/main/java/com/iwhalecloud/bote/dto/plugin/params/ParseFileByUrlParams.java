package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 根据地址解析内容
 *
 * @author chen.linfa
 * @since 2025-11-14
 */
@Getter
@Setter
@ToString
public class ParseFileByUrlParams extends AbstractPluginParams {

  private String url;

  public ParseFileByUrlParams() {
    super(PluginConsts.PLUGIN_CODE_PARSE_FILE_BY_URL);
  }

}
