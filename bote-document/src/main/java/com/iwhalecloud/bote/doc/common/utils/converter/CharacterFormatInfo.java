package com.iwhalecloud.bote.doc.common.utils.converter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 字符格式信息
 */
@Getter
@Setter
@ToString
public final class CharacterFormatInfo {
  private Boolean isBold;
  private Boolean isItalic;
  private Boolean isUnderline;
  private Boolean isStrikethrough;
  private Short subSuperScript;
  private String fontName;
  private Integer fontSize;
  private String fontColor;
}
