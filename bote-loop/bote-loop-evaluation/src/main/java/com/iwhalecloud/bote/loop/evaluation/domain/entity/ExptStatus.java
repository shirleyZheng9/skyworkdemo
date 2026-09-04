package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 实验状态枚举
 * 迁移对应关系: Go语言entity.ExptStatus
 * - 功能: 定义实验的执行状态
 * - 用途: 实验状态管理和业务逻辑判断
 * <p>
 * Java实现说明:
 * - 对应Go的ExptStatus枚举
 * - 提供值转换方法
 * - 支持状态判断
 * <p>
 * 技术栈迁移:
 * - Go type ExptStatus int64 -> Java enum ExptStatus
 * - Go int64值 -> Java Long值
 */
public enum ExptStatus {

  /**
   * 未知状态
   * 迁移对应关系: Go语言ExptStatus_Unknown
   * - 值: 0
   * - 功能: 未知的实验状态
   */
  UNKNOWN(0L),

  /**
   * 等待执行
   * 迁移对应关系: Go语言ExptStatus_Pending
   * - 值: 2
   * - 功能: 实验等待执行
   */
  PENDING(2L),

  /**
   * 执行中
   * 迁移对应关系: Go语言ExptStatus_Processing
   * - 值: 3
   * - 功能: 实验正在执行中
   */
  PROCESSING(3L),

  /**
   * 执行成功
   * 迁移对应关系: Go语言ExptStatus_Success
   * - 值: 11
   * - 功能: 实验执行成功
   */
  SUCCESS(11L),

  /**
   * 执行失败
   * 迁移对应关系: Go语言ExptStatus_Failed
   * - 值: 12
   * - 功能: 实验执行失败
   */
  FAILED(12L),

  /**
   * 用户终止
   * 迁移对应关系: Go语言ExptStatus_Terminated
   * - 值: 13
   * - 功能: 用户主动终止实验
   */
  TERMINATED(13L),

  /**
   * 系统终止
   * 迁移对应关系: Go语言ExptStatus_SystemTerminated
   * - 值: 14
   * - 功能: 系统终止实验
   */
  SYSTEM_TERMINATED(14L),

  /**
   * 流式执行完成
   * 迁移对应关系: Go语言ExptStatus_Draining
   * - 值: 21
   * - 功能: 流式执行完成，不再接收新的请求
   */
  DRAINING(21L);

  private final Long value;

  ExptStatus(Long value) {
    this.value = value;
  }

  /**
   * 获取枚举值
   * 迁移对应关系: Go语言ExptStatus的值
   *
   * @return 枚举值
   */
  public Long getValue() {
    return value;
  }

  /**
   * 根据值获取枚举
   * 迁移对应关系: Go语言ExptStatus的值转换
   *
   * @param value 枚举值
   * @return 对应的枚举
   */
  public static ExptStatus fromValue(Long value) {
    if (value == null) {
      return null;
    }
    for (ExptStatus status : values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("未知的ExptStatus值: " + value);
  }

  /**
   * 判断实验是否已完成
   * 迁移对应关系: Go语言IsExptFinished
   *
   * @param status 实验状态
   * @return 是否已完成
   */
  public static boolean isExptFinished(ExptStatus status) {
    if (status == null) {
      return false;
    }
    return status == SUCCESS || status == FAILED || status == TERMINATED || status == SYSTEM_TERMINATED;
  }
}
