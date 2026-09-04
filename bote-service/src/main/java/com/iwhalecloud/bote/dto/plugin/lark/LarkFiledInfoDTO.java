package com.iwhalecloud.bote.dto.plugin.lark;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书字段信息
 *
 * @author qian.sisheng
 * @since 2025-08-23
 */
@Getter
@Setter
@ToString
public class LarkFiledInfoDTO {
  /** 字段名称 */
  private String fieldName;
  /** 字段类型 */
  private String type;
  /** 字段描述 */
  private String description;
  /** 是否是索引列 */
  private Boolean isPrimary;
  /** 字段ID */
  private String fieldId;
  /** 字段在界面上的展示类型 */
  private String uiType;
  /** 是否是隐藏字段 */
  private Boolean isHidden;
  /** 字段属性 */
  private LarkFieldPropertyDTO property;
}
