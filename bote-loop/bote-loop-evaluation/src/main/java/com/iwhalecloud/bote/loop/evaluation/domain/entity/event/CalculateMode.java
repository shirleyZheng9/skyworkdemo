package com.iwhalecloud.bote.loop.evaluation.domain.entity.event;

public enum CalculateMode {
  CREATE_ALL_FIELDS(1),
  UPDATE_SPECIFIC_FIELD(2);

  private final int value;

  CalculateMode(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
