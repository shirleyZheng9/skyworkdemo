package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网页内容提取参数
 *
 * @author zhangJun
 * @since 2025-07-18
 */

@Getter
@Setter
@ToString
public class Nl2sqlPluginParams extends AbstractPluginParams {
  /** 数据源id */
  private Long dataSourceId;
  /** 用户查询内容 */
  private String query;

  public Nl2sqlPluginParams() {
    super(PluginConsts.PLUGIN_CODE_IMAGE_NL2SQL);
  }
}
