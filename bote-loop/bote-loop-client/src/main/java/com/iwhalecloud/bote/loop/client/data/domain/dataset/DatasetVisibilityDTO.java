package com.iwhalecloud.bote.loop.client.data.domain.dataset;

/**
 * Dataset Visibility Enum
 */
public enum DatasetVisibilityDTO {
  UNKNOWN(0, ""),
  PUBLIC(1, "Public"),
  SPACE(2, "Space"),
  SYSTEM(3, "System");

  private final int value;
  private final String name;

  DatasetVisibilityDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }
}
