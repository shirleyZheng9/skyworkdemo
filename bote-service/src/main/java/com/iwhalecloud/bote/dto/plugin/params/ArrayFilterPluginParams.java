package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数组筛选器插件参数
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class ArrayFilterPluginParams extends AbstractPluginParams {
  /** 待筛选的数组（必填，List或数组类型） */
  private Object array;
  /** 字段名（可选，字符串类型），如果数组元素是对象，则通过此字段访问属性；如果为null或空字符串，则直接比较数组元素本身 */
  private String fieldName;
  /** 操作符（必填，字符串类型），可选值：gt（大于）, gte（大于等于）, lt（小于）, lte（小于等于）, eq（等于）, contains（包含）, notContains（不包含） */
  private String operator;
  /** 比较值（必填，任意类型），用于与数组元素进行比较的值 */
  private Object value;

  public ArrayFilterPluginParams() {
    super(PluginConsts.PLUGIN_CODE_ARRAY_FILTER);
  }
}

