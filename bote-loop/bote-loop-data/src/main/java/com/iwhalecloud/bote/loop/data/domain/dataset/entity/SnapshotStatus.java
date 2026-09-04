package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 快照状态枚举
 * 迁移对应关系: Go语言entity.SnapshotStatus
 * - 功能: 快照状态标识
 * - 字段定义: 各种快照状态常量
 * <p>
 * Java实现说明:
 * - 对应Go的SnapshotStatus类型别名
 * - 使用Java枚举定义各种快照状态
 * - 提供值和名称的访问方法
 * - 包含完成状态判断方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 * - Go方法 -> Java方法
 */
public enum SnapshotStatus {
  UNKNOWN(""),
  UNSTARTED("unstarted"),
  IN_PROGRESS("in_progress"),
  COMPLETED("completed"),
  FAILED("failed");

  private final String value;

  SnapshotStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static SnapshotStatus fromValue(String value) {
    for (SnapshotStatus status : values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    return UNKNOWN;
  }

  /**
   * 判断是否为完成状态
   * 迁移对应关系: Go语言entity.SnapshotStatus.IsFinished()
   * - 功能: 判断快照状态是否已完成
   * - 返回: 是否为完成状态
   */
  public boolean isFinished() {
    return this == COMPLETED || this == FAILED;
  }
}
