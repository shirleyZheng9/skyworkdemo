package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书多维表格批量删除记录插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-26
 */
@Getter
@Setter
@ToString
public class LarkDeleteRecordsPluginParams extends AbstractLarkPluginParams {
  /** 删除的记录ID列表 */
  private List<String> records;

  public LarkDeleteRecordsPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_DELETE_RECORDS, "飞书多维表格批量删除记录插件");
  }
}
