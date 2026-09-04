package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * SQL 参数说明
 *
 * @author bianjp
 * @since 2025-11-25
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class SqlParameterSpec {
  /** 参数名称 */
  private String name;
  /** 描述 */
  private String description;
  /** 数据类型 */
  private String dataType;
  /** 来源值（常量值、变量名称等） */
  private String value;
  /** 默认值 */
  private String defaultValue;
  /** 数据源编码（dataRuleType=seq 时使用） */
  @JsonIgnore
  private String dataSourceCode;
  /** 数据规则类型（后端在转换插入记录节点时使用，前端不会使用） */
  private String dataRuleType;
  /** 数据规则对象 */
  private String dataRuleObj;
  /** 操作类型 */
  private String operator;
  /** 是否必填 */
  private Boolean required;

  public SqlParameterSpec(SqlParameterSpec spec) {
    this.name = spec.name;
    this.description = spec.description;
    this.dataType = spec.dataType;
    this.value = spec.value;
    this.defaultValue = spec.defaultValue;
    this.dataSourceCode = spec.dataSourceCode;
    this.dataRuleType = spec.dataRuleType;
    this.dataRuleObj = spec.dataRuleObj;
    this.operator = spec.operator;
    this.required = spec.required;
  }
}
