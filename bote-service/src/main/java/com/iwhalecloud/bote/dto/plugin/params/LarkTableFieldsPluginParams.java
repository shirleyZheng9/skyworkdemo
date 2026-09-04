package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书多维表格查询数据表表字段插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-22
 */
@Getter
@Setter
@ToString
public class LarkTableFieldsPluginParams extends AbstractLarkPluginParams {

  /** 分页标记，第一次请求不填，表示从头开始遍历；分页查询结果还有更多项时会同时返回新的 page_token，下次遍历可采用该 page_token 获取查询结果 */
  private String pageToken;
  /** 分页大小，默认值20，最大值 100 */
  private String pageSize;


  public LarkTableFieldsPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_TABLE_FIELDS, "飞书多维表格查询数据表表字段插件");
  }
}
