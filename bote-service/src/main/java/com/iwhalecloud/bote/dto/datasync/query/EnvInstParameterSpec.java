package com.iwhalecloud.bote.dto.datasync.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 可变更环境实例规格
 *
 * @author chen.linfa
 * @since 2024-10-31
 */
@Getter
@Setter
@ToString
public class EnvInstParameterSpec {
  /** 表字段编码 */
  private String code;
  /** 表字段名称 */
  private String name;
  /** 字段值 */
  private String value;

  /** 主键字段编码 */
  private String primaryKey;
  /** 主键字段值，定义为字符型，避免精度丢失 */
  private String primaryKeyValue;

  /** 父节点编码 */
  private String parentCode;
  /** 父节点名称 */
  private String parentName;

  /** 数据同步模块 */
  private String dataSyncCode;
  /** 表编码 */
  private String tableCode;

  /** 分组key */
  private String groupKey;
  /** 分组名 */
  private String groupName;
}
