package com.iwhalecloud.bote.loop.client.data.domain.dataset;

/**
 * 存储提供商枚举
 */
public enum StorageProviderDTO {
  TOS(1, "TOS"),
  VETOS(2, "VETOS"),
  HDFS(3, "HDFS"),
  IMAGEX(4, "ImageX"),
  S3(5, "S3"),
  ABASE(100, "Abase"),
  RDS(101, "RDS"),
  LOCALFS(102, "LocalFS");

  private final int value;
  private final String name;

  StorageProviderDTO(int value, String name) {
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
