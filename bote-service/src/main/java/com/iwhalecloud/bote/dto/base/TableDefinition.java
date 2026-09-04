package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 代码生成 - 表定义
 *
 * @author chen.linfa
 * @since 2024-09-12
 */
@Getter
@Setter
@ToString
@Builder
public class TableDefinition {
  /** 表编码 */
  private String tableCode;
  /** 实体名称，作为备注 */
  private String entityDesc;
  /** 实体编码，用于构造模型对应的文件，例如：${entityCode}+Entity ${entityCode}+DTO ${entityCode}+QueryParams */
  private String entityCode;
  /** 是否含有日期型字段，用于选择性 import java.util.Date */
  private Boolean containDateColumn;
  /** 表主键序列编码 */
  private String sequenceCode;
  /** 表字段定义 */
  private List<TableColumnDefinition> columns;

  /**
   * 是否带有时间字段（除了基础字段）
   */
  @JsonIgnore
  public boolean hasDateColumn() {
    return BooleanUtils.isTrue(containDateColumn);
  }
}
