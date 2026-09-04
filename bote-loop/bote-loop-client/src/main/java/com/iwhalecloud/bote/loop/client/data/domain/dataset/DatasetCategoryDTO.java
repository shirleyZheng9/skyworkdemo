package com.iwhalecloud.bote.loop.client.data.domain.dataset;

/**
 * Dataset Category Enum
 */
public enum DatasetCategoryDTO {
  UNKNOWN(0, ""),
  GENERAL(1, "General"),
  TRAINING(2, "Training"),
  VALIDATION(3, "Validation"),
  EVALUATION(4, "Evaluation");

  private final int value;
  private final String name;

  DatasetCategoryDTO(int value, String name) {
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
