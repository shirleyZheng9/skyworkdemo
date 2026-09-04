package com.iwhalecloud.bote.loop.evaluation.domain.entity.event;

public enum UpsertExptTurnResultFilterType {
  AUTO("auto"),
  CHECK("check"),
  MANUAL("manual");

  private final String value;

  UpsertExptTurnResultFilterType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
