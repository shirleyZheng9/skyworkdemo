package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

/**
 * 模板类型枚举
 * 对应Thrift: TemplateType
 */
public enum TemplateTypeDTO {
  NORMAL("normal");

  private final String value;

  TemplateTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TemplateTypeDTO fromValue(String value) {
    for (TemplateTypeDTO type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown TemplateType: " + value);
  }
}
