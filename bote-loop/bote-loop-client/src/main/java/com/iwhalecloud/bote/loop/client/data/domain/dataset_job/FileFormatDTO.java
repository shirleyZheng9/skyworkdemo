package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

/**
 * 文件格式枚举
 */
public enum FileFormatDTO {
  JSONL(1, "JSONL"),
  PARQUET(2, "Parquet"),
  CSV(3, "CSV"),
  ZIP(100, "ZIP");

  private final int value;
  private final String name;

  FileFormatDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static FileFormatDTO fromValue(int value) {
    for (FileFormatDTO format : values()) {
      if (format.value == value) {
        return format;
      }
    }
    throw new IllegalArgumentException("Unknown file format: " + value);
  }
}
