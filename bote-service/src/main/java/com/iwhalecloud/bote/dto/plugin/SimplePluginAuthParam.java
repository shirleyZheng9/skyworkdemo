package com.iwhalecloud.bote.dto.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 插件鉴权信息参数
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
public class SimplePluginAuthParam {
  /** 类型 */
  private String type;
  /** 属性名称 */
  private String name;
  /** 属性编码 */
  private String code;
  /** 属性值 */
  private String value;
  /** 描述 */
  private String remark;
}
