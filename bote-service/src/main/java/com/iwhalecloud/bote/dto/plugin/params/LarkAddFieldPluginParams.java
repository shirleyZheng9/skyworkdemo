package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书多为表格添加字段插件参数
 *
 * @author qian.sisheng
 * @since 2025-08-23
 */
@Getter
@Setter
@ToString
public class LarkAddFieldPluginParams extends AbstractLarkPluginParams {

  /** 字段名称 */
  private String fieldName;
  /** 字段类型 */
  private Integer type;
  /** 字段在界面上的展示类型 */
  private String uiType;
  /** 字段描述 */
  private LarkFieldDescriptionDTO description;

  public LarkAddFieldPluginParams() {
    super(PluginConsts.PLUGIN_CODE_LARK_ADD_FIELDS, "飞书多为表格添加字段插件");
  }

  @Getter
  @Setter
  @ToString
  public static class LarkFieldDescriptionDTO {
    /** 是否禁止同步该描述，只在新增、修改字段时生效。 true：表示禁止同步该描述内容到表单的问题描述 false：允许同步该描述 */
    private boolean disableSync;
    /** 字段描述内容 */
    private String text;
  }
}
