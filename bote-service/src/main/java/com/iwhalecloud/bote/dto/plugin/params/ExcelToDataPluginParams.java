package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;

/**
 * 读取Excel文件并转换成数据
 *
 * @author fan.cong
 * @since 2025-08-12
 */
@Getter
@Setter
public class ExcelToDataPluginParams extends AbstractPluginParams {
  /**
   * 文件ID
   */
  private Long fileId;

  /**
   * 返回数据类型
   */
  private String dataType;

  public ExcelToDataPluginParams() {
    super(PluginConsts.PLUGIN_CODE_EXCEL_TO_DATA);
  }
}
