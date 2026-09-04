package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.params.LarkAddFieldPluginParams.LarkFieldDescriptionDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书多维表格修改表格字段插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-26
 */
@Getter
@Setter
@ToString
public class LarkUpdateFieldPluginParams extends AbstractLarkPluginParams {

  /** 字段ID */
  private String fieldId;
  /** 字段名称 */
  private String fieldName;
  /** 字段类型 */
  private Integer type;
  /** 字段在界面上的展示类型 */
  private String uiType;
  /** 字段描述 */
  private LarkFieldDescriptionDTO description;

  public LarkUpdateFieldPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_UPDATE_FIELD, "飞书多维表格修改表格字段插件");
  }
}
