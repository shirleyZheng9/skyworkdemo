package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书创建多维表格数据表插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-27
 */
@Getter
@Setter
@ToString
public class LarkCreateTablePluginParams extends AbstractLarkPluginParams {

  /** 数据表名称 */
  private String name;
  /** 字段列表 */
  private List<FieldDTO> fields;

  public LarkCreateTablePluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_CREATE_TABLE, "飞书创建多维表格数据表插件");
  }

  @Getter
  @Setter
  @ToString
  public static class FieldDTO {
    /** 字段名称 */
    private String fieldName;
    /** 字段类型 */
    private Integer type;
  }
}
