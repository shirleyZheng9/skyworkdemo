package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import lombok.Getter;

/**
 * 任务状态枚举
 * 迁移对应关系: Go语言JobStatus
 * - 功能: 定义IO任务的状态
 * - 值范围: 0-5
 * <p>
 * Java实现说明:
 * - 对应Go的JobStatus类型
 * - 使用Java枚举定义，包含状态值和方法
 * - 提供状态转换和判断功能
 * <p>
 * 技术栈迁移:
 * - Go常量 -> Java枚举
 * - Go方法 -> Java方法
 * - Go字符串转换 -> JavatoString方法
 */
public enum JobStatus {

  /**
   * 未定义状态
   * 迁移对应关系: Go语言JobStatus_Undefined
   * - 值: 0
   * - 功能: 未定义状态
   */
  UNDEFINED(0, "Undefined"),

  /**
   * 待处理状态
   * 迁移对应关系: Go语言JobStatus_Pending
   * - 值: 1
   * - 功能: 任务等待处理
   */
  PENDING(1, "Pending"),

  /**
   * 处理中状态
   * 迁移对应关系: Go语言JobStatus_Running
   * - 值: 2
   * - 功能: 任务正在处理中
   */
  RUNNING(2, "Running"),

  /**
   * 已完成状态
   * 迁移对应关系: Go语言JobStatus_Completed
   * - 值: 3
   * - 功能: 任务已完成
   */
  COMPLETED(3, "Completed"),

  /**
   * 失败状态
   * 迁移对应关系: Go语言JobStatus_Failed
   * - 值: 4
   * - 功能: 任务执行失败
   */
  FAILED(4, "Failed"),

  /**
   * 已取消状态
   * 迁移对应关系: Go语言JobStatus_Cancelled
   * - 值: 5
   * - 功能: 任务已被取消
   */
  CANCELLED(5, "Cancelled");

  /**
   * -- GETTER --
   * 获取状态值
   * 迁移对应关系: Go语言JobStatus的值
   * - 功能: 获取状态的数值表示
   * - 返回: 状态值
   */
  @Getter
  private final int value;
  @Getter
  private final String displayName;

  JobStatus(int value, String displayName) {
    this.value = value;
    this.displayName = displayName;
  }

  /**
   * 获取显示名称
   * 迁移对应关系: Go语言JobStatus.String()
   * - 功能: 获取状态的字符串表示
   * - 返回: 状态显示名称
   */
  @Override
  public String toString() {
    return displayName;
  }

  /**
   * 根据字符串获取状态
   * 迁移对应关系: Go语言JobStatusFromString
   * - 功能: 根据字符串获取对应的状态
   * - 参数: s - 状态字符串
   * - 返回: 对应的状态枚举
   * - 异常: 如果字符串无效则抛出异常
   */
  public static JobStatus fromString(String s) {
    if (s == null) {
      throw new IllegalArgumentException("JobStatus string cannot be null");
    }

    for (JobStatus status : values()) {
      if (status.displayName.equals(s)) {
        return status;
      }
    }

    throw new IllegalArgumentException("not a valid JobStatus string: " + s);
  }

  /**
   * 根据值获取状态
   * 迁移对应关系: Go语言JobStatus的值转换
   * - 功能: 根据数值获取对应的状态
   * - 参数: value - 状态值
   * - 返回: 对应的状态枚举
   * - 异常: 如果值无效则抛出异常
   */
  public static JobStatus fromValue(int value) {
    for (JobStatus status : values()) {
      if (status.value == value) {
        return status;
      }
    }

    throw new IllegalArgumentException("not a valid JobStatus value: " + value);
  }

  /**
   * 判断是否为终端状态
   * 迁移对应关系: Go语言IsJobTerminal
   * - 功能: 判断任务是否已到达终端状态
   * - 返回: 是否为终端状态
   * - 用途: 判断任务是否可以继续处理
   */
  public boolean isTerminal() {
    return this == COMPLETED || this == FAILED || this == CANCELLED;
  }

  /**
   * 判断是否为终端状态（静态方法）
   * 迁移对应关系: Go语言IsJobTerminal
   * - 功能: 判断指定状态是否为终端状态
   * - 参数: status - 要判断的状态
   * - 返回: 是否为终端状态
   */
  public static boolean isJobTerminal(JobStatus status) {
    return status != null && status.isTerminal();
  }
}
