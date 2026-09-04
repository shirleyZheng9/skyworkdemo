package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列评估集字段实体
 * 对应Go: entity.ColumnEvalSetField
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnEvalSetField {

  /**
   * 字段键
   * 对应Go: Key *string
   */
  private String key;

  /**
   * 字段名称
   * 对应Go: Name *string
   */
  private String name;

  /**
   * 字段描述
   * 对应Go: Description *string
   */
  private String description;

  /**
   * 内容类型
   * 对应Go: ContentType ContentType
   */
  private ContentType contentType;

  /**
   * 文本模式
   * 对应Go: TextSchema *string
   */
  private String textSchema;
}
