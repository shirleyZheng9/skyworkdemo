package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 模板类型枚举
 * 迁移对应关系: Thrift TemplateType
 */
@Getter
public enum TemplateTypeDTO {
  @JsonProperty("normal")
  NORMAL("normal");

  private final String value;

  TemplateTypeDTO(String value) {
    this.value = value;
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
