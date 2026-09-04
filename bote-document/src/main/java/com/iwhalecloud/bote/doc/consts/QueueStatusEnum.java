package com.iwhalecloud.bote.doc.consts;

/**
 * 队列状态枚举
 */
public enum QueueStatusEnum {

  /**
   * 任务在队列中等待处理
   */
  PENDING("PENDING", "待处理"),

  /**
   * 任务正在执行中
   */
  PROCESSING("PROCESSING", "处理中"),

  /**
   * 任务执行成功完成
   */
  COMPLETED("COMPLETED", "已完成"),

  /**
   * 任务执行失败
   */
  FAILED("FAILED", "失败"),

  /**
   * 任务执行超时被强制终止
   */
  TIMEOUT("TIMEOUT", "超时");

  private final String code;
  private final String description;

  QueueStatusEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
}
