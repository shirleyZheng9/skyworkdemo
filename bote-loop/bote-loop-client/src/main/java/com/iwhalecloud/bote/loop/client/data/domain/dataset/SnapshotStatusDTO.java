package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import java.util.Objects;

/**
 * 快照状态枚举
 */
public enum SnapshotStatusDTO {
  UNSTARTED(1, "Unstarted"),
  IN_PROGRESS(2, "InProgress"),
  COMPLETED(3, "Completed"),
  FAILED(4, "Failed");

  private final int value;
  private final String name;

  SnapshotStatusDTO(int value, String name) {
    this.value = value;
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static SnapshotStatusDTO fromValue(String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("SnapshotStatusDTO name cannot be null or empty");
    }
    // 不区分大小写匹配
    for (SnapshotStatusDTO snapshotStatusDTO : values()) {
      if (Objects.equals(snapshotStatusDTO.name, name) ||
          snapshotStatusDTO.name.equalsIgnoreCase(name)) {
        return snapshotStatusDTO;
      }
    }
    throw new IllegalArgumentException("Unknown SnapshotStatusDTO: " + name);
  }
}
