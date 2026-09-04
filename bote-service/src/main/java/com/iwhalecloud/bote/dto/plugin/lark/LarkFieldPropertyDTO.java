package com.iwhalecloud.bote.dto.plugin.lark;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 字段属性
 *
 * @author qian.sisheng
 * @since 2025-08-23
 */
@Getter
@Setter
@ToString
public class LarkFieldPropertyDTO {
  /** 字段选项 */
  private List<LarkFieldOptionDTO> options;
  /** 数字、公式字段的显示格式 */
  private String formatter;
  /** 日期、创建时间、最后更新时间字段的显示格式 */
  private String dateFormatter;
  /** 日期字段中新纪录自动填写创建时间 */
  private boolean autoFill;
  /** 人员字段中允许添加多个成员，单向关联、双向关联中允许添加多个记录 */
  private boolean multiple;
  /** 单向关联、双向关联字段中关联的数据表的ID */
  private String tableId;
  /** 单向关联、双向关联字段中关联的数据表的名字 */
  private String tableName;
  /** 双向关联字段中关联的数据表中对应的双向关联字段的名字 */
  private String backFieldName;
  /** 自动编号类型 */
  private LarkFieldAutoSerialDTO autoSerial;
  /** 公式字段的表达式 */
  private String formulaExpression;
  /** 地理位置输入方式 */
  private LarkFieldLocationDTO location;
  /** 字段支持的编辑模式 */
  private LarkFieldAllowedEditModesDTO allowedEditModes;
  /** 进度、评分等字段的数据范围最小值 */
  private BigDecimal min;
  /** 进度、评分等字段的数据范围最大值 */
  private BigDecimal max;
  /** 进度等字段是否支持自定义范围 */
  private boolean rangeCustomize;
  /** 货币币种 */
  private String currencyCode;
  /** 评分字段的相关设置 */
  private LarkFieldRatingDTO rating;
  /** 设置公式字段的数据类型 */
  private LarkFieldTypeDTO type;
  /** 查找条件 */
  private LarkFieldFilterInfoDTO filterInfo;

  @Getter
  @Setter
  @ToString
  private static final class LarkFieldOptionDTO {
    /** 选项名 */
    private String name;
    /** 选项ID，创建时不允许指定ID */
    private String id;
    /** 选项颜色 */
    private Integer color;
  }

  /**
   * 自动编号类型
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldAutoSerialDTO {
    /** 自动编号类型 */
    private String type;
    /** 自动编号规则列表 */
    private List<LarkFieldAutoSerialRuleDTO> options;
  }

  /**
   * 自动编号规则列表
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldAutoSerialRuleDTO {
    /** 自动编号的可选规则项类型 */
    private String type;
    /** 与自动编号的可选规则项类型相对应的取值 */
    private String value;
  }

  /**
   * 地理位置输入方式
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldLocationDTO {
    /** 地理位置输入限制 */
    private String inputType;
  }

  /**
   * 字段支持的编辑模式
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldAllowedEditModesDTO {
    /** 是否允许手动录入 */
    private boolean manual;
    /** 是否允许移动端录入 */
    private boolean scan;
  }

  /**
   * 评分字段的相关设置
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldRatingDTO {
    /** 评分字段的符号展示 */
    private String symbol;
  }

  /**
   * 设置公式字段的数据类型
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldTypeDTO {
    /** 公式字段对应的数据类型 */
    private String dataType;
    /** 公式数据属性 */
    private LarkFieldUiPropertyDTO uiProperty;
  }

  /**
   * 公式数据属性
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldUiPropertyDTO {
    /** 货币币种 */
    private String currencyCode;
    /** 数字、公式字段的显示格式 */
    private String formatter;
    /** 进度等字段是否支持自定义范围 */
    private boolean rangeCustomize;
    /** 进度、评分等字段的数据范围最小值 */
    private BigDecimal min;
    /** 进度、评分等字段的数据范围最大值 */
    private BigDecimal max;
    /** 日期、创建时间、最后更新时间字段的显示格式 */
    private String dateFormatter;
    /** 评分字段的相关设置 */
    private LarkFieldRatingDTO rating;
    /** 公式字段在界面上的展示类型 */
    private String uiType;
  }

  /**
   * 查找引用关系
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldFilterInfoDTO {
    /** 引用表格 */
    private String targetTable;
    /** 查找条件 */
    private LarkFieldFilterDTO filterInfo;
  }

  /**
   * 查找条件
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldFilterDTO {
    /** 多个筛选条件的关系 */
    private String conjunction;
    /** 筛选条件列表 */
    private List<LarkFieldConditionDTO> conditions;
  }

  /**
   * 筛选条件
   */
  @Getter
  @Setter
  @ToString
  private static final class LarkFieldConditionDTO {
    /** 用于过滤的字段唯一ID */
    private String fieldId;
    /** 过滤操作的类型 */
    private String operator;
    /** 筛选值 */
    private String value;
    /** 过滤条件的唯一ID */
    private String conditionId;
    /** 用于过滤的字段类型 */
    private Integer fieldType;
  }

}
