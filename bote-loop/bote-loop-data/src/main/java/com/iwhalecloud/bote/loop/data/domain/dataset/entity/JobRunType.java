package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

/**
 * 任务运行类型枚举
 * 迁移对应关系: Go语言entity.JobRunType
 * - 功能: 任务运行类型标识
 * - 字段定义: 各种运行类型常量
 * <p>
 * Java实现说明:
 * - 对应Go的JobRunType类型别名
 * - 使用Java枚举定义各种运行类型
 * - 提供值和名称的访问方法
 * <p>
 * 技术栈迁移:
 * - Go类型别名 -> Java枚举
 * - Go常量 -> Java枚举值
 */
public enum JobRunType {
  DATASET_IO_JOB("dataset_io_job"),
  DATASET_SNAPSHOT_JOB("dataset_snapshot_job");

  private final String value;

  JobRunType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static JobRunType fromValue(String value) {
    for (JobRunType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown job run type: " + value);
  }
}
