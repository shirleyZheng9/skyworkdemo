package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书搜索多维表格文档插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-26
 */
@Getter
@Setter
@ToString
public class LarkSearchDocPluginParams extends AbstractLarkPluginParams {
  /** 搜索数量, 范围0-50 */
  private String count;
  /** 指定搜索的偏移量，该参数最小为 0，即不偏移。该参数的值与返回的文件数量之和不得大于或等于 200 即 offset + count < 200） */
  private String offset;
  /** 搜索关键字 */
  private String searchKey;

  public LarkSearchDocPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_SEARCH_DOC, "飞书搜索多维表格文档插件");
  }
}
