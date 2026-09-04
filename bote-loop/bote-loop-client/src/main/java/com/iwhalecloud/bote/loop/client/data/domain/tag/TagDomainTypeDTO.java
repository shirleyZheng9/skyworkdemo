package com.iwhalecloud.bote.loop.client.data.domain.tag;

/**
 * 标签领域类型枚举
 */
public enum TagDomainTypeDTO {
  DATA("data"),
  OBSERVE("observe"),
  EVALUATION("evaluation");

  private final String value;

  TagDomainTypeDTO(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
