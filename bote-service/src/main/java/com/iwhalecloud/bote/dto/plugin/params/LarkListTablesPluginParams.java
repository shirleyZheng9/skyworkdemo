package com.iwhalecloud.bote.dto.plugin.params;


import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书获多维表格取全部数据表插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-22
 */
@Getter
@Setter
@ToString
public class LarkListTablesPluginParams extends AbstractLarkPluginParams {

  /** 分页标记，第一次请求不填，后续请求填上一次请求的 page_token */
  private String pageToken;
  /** 每页大小，默认20, 最大为 100 */
  private String pageSize;

  public LarkListTablesPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_LIST_TABLES, "飞书获多维表格取全部数据表插件");
  }
}
