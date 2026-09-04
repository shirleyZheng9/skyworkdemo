package com.iwhalecloud.bote.loop.client.data.domain.dataset;

/**
 * Security Level Enum
 */
public enum SecurityLevelDTO {
  UNKNOWN(0, ""),
  L1(1, "L1"),
  L2(2, "L2"),
  L3(3, "L3"),
  L4(4, "L4");

  private final int value;
  private final String name;

  SecurityLevelDTO(int value, String name) {
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
