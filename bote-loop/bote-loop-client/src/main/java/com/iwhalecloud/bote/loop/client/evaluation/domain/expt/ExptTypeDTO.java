package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 实验类型枚举
 */
@Getter
public enum ExptTypeDTO {
  @JsonProperty("1")
  OFFLINE(1, "Offline"),
  @JsonProperty("2")
  ONLINE(2, "Online");

  private final int value;
  private final String name;

  ExptTypeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public static ExptTypeDTO fromValue(int value) {
    for (ExptTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown expt type: " + value);
  }
}
