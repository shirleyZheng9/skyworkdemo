package com.iwhalecloud.bote.dto.base;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 代码生成 - 表字段定义
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
@Getter
@Setter
@ToString
@Builder
public class TableColumnDefinition {
  /** 表字段名 */
  private String columnName;
  /** 表字段类型 */
  private String columnType;
  /** 是否主键 */
  private Boolean isPrimaryKey;
  /** 是否日期型 */
  private Boolean isDateType;
  /** 类字段名 */
  private String paramName;
  /** 类字段类型 */
  private String paramType;
  /** 数据长度 */
  private String dataLength;
  /** 默认值 */
  private String defaultValue;
  /** 注释 */
  private String comment;

  /** 基础字段标识，用于 mapper.xml */
  private Boolean isBaseKey;
  private Boolean isCreatorIdKey;
  private Boolean isUpdatorIdKey;
  private Boolean isCreatedTimeKey;
  private Boolean isUpdatedTimeKey;
}
