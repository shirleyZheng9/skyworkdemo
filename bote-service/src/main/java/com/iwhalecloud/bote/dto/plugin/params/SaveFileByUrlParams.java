package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 根据地址保存图片
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString
public class SaveFileByUrlParams extends AbstractPluginParams {
  /**
   * url
   */
  private String url;

  public SaveFileByUrlParams() {
    super(PluginConsts.PLUGIN_CODE_SAVE_FILE_BY_URL);
  }

}
