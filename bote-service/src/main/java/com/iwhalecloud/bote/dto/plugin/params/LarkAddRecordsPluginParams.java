package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书多维表格批量添加记录插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-25
 */
@Getter
@Setter
@ToString
public class LarkAddRecordsPluginParams extends AbstractLarkPluginParams {

  /** 批量添加的记录 */
  private List<RecordDTO> records;

  public LarkAddRecordsPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_ADD_RECORDS, "飞书多维表格批量添加记录插件");
  }

  @Getter
  @Setter
  @ToString
  public static class RecordDTO {
    /** 字段列表，页面使用时使用JSON字符串，如 {\"文本\": \"文本内容\", \"单选\": \"选项1\"} */
    private Object fields;
  }
}
