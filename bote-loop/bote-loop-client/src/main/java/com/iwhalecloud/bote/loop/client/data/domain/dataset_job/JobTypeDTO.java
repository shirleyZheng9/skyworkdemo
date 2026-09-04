package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

/**
 * 任务类型枚举
 */
public enum JobTypeDTO {
  IMPORT_FROM_FILE(1, "ImportFromFile"),
  EXPORT_TO_FILE(2, "ExportToFile"),
  EXPORT_TO_DATASET(3, "ExportToDataset");

  private final int value;
  private final String name;

  JobTypeDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static JobTypeDTO fromValue(int value) {
    for (JobTypeDTO type : values()) {
      if (type.value == value) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown job type: " + value);
  }
}
