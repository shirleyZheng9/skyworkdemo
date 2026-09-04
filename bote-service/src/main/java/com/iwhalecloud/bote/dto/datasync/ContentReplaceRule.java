package com.iwhalecloud.bote.dto.datasync;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 表字段内容替换规则
 *
 * @author chen.linfa
 * @since 2025-05-26
 */
@Getter
@Setter
@ToString
public class ContentReplaceRule {
  /** 表字段编码 */
  private String columnCode;
  /** 键值替换规则 */
  private String rule;
  /** 表字段类型 */
  private String dataType;
  /** 关联的表 */
  private String relatedTable;
}
