package com.iwhalecloud.bote.common.lock;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.Getter;

/**
 * 分布式锁通用异常类
 *
 * <p>用于封装分布式锁操作过程中可能出现的各种异常情况。</p>
 *
 * <p>常见的异常场景包括：</p>
 * <ul>
 *   <li>锁服务连接异常（Zookeeper、Redis等）</li>
 *   <li>锁获取/释放失败</li>
 *   <li>网络中断导致的操作失败</li>
 *   <li>锁路径/键创建失败</li>
 *   <li>锁配置错误</li>
 *   <li>锁超时异常</li>
 * </ul>
 *
 * @since 2025-08-21
 */
@Getter
public class DistributedLockException extends BssException {

  private static final long serialVersionUID = 1L;

  /**
   * 异常错误码
   * -- GETTER --
   * 获取错误码
   */
  private final String errorCode;

  /**
   * 锁标识
   * -- GETTER --
   * 获取锁标识
   */
  private final String lockKey;

  /**
   * 构造函数
   *
   * @param message 异常消息
   */
  public DistributedLockException(String message) {
    super(message);
    this.errorCode = null;
    this.lockKey = null;
  }

  /**
   * 构造函数
   *
   * @param message 异常消息
   * @param cause 引起此异常的原因
   */
  public DistributedLockException(String message, Throwable cause) {
    super(message, cause);
    this.errorCode = null;
    this.lockKey = null;
  }

  /**
   * 构造函数
   *
   * @param cause 引起此异常的原因
   */
  public DistributedLockException(Throwable cause) {
    super(cause);
    this.errorCode = null;
    this.lockKey = null;
  }

  /**
   * 构造函数（带错误码）
   *
   * @param errorCode 错误码
   * @param message 异常消息
   */
  public DistributedLockException(String errorCode, String message) {
    super(errorCode, message);
    this.errorCode = errorCode;
    this.lockKey = null;
  }

  /**
   * 构造函数（带错误码和锁标识）
   *
   * @param errorCode 错误码
   * @param message 异常消息
   * @param lockKey 锁标识
   */
  public DistributedLockException(String errorCode, String message, String lockKey) {
    super(errorCode, message);
    this.errorCode = errorCode;
    this.lockKey = lockKey;
  }

  /**
   * 构造函数（带错误码和原因）
   *
   * @param errorCode 错误码
   * @param message 异常消息
   * @param cause 引起此异常的原因
   */
  public DistributedLockException(String errorCode, String message, Throwable cause) {
    super(errorCode, message, cause);
    this.errorCode = errorCode;
    this.lockKey = null;
  }

  /**
   * 构造函数（完整参数）
   *
   * @param errorCode 错误码
   * @param message 异常消息
   * @param lockKey 锁标识
   * @param cause 引起此异常的原因
   */
  public DistributedLockException(String errorCode, String message, String lockKey, Throwable cause) {
    super(errorCode, message, cause);
    this.errorCode = errorCode;
    this.lockKey = lockKey;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    if (errorCode != null) {
      sb.append("[").append(errorCode).append("]");
    }
    if (lockKey != null) {
      sb.append("[").append(lockKey).append("]");
    }
    sb.append(": ").append(getMessage());
    return sb.toString();
  }

  /**
   * 常见错误码常量
   */
  public static final class ErrorCodes {
    /** 连接失败 */
    public static final String CONNECTION_FAILED = "LOCK_CONNECTION_FAILED";

    /** 获取锁超时 */
    public static final String ACQUIRE_TIMEOUT = "LOCK_ACQUIRE_TIMEOUT";

    /** 释放锁失败 */
    public static final String RELEASE_FAILED = "LOCK_RELEASE_FAILED";

    /** 锁已被占用 */
    public static final String ALREADY_LOCKED = "LOCK_ALREADY_LOCKED";

    /** 锁不存在 */
    public static final String NOT_EXISTS = "LOCK_NOT_EXISTS";

    /** 配置错误 */
    public static final String CONFIG_ERROR = "LOCK_CONFIG_ERROR";

    /** 权限不足 */
    public static final String PERMISSION_DENIED = "LOCK_PERMISSION_DENIED";

    /** 服务不可用 */
    public static final String SERVICE_UNAVAILABLE = "LOCK_SERVICE_UNAVAILABLE";

    /** 未知错误 */
    public static final String UNKNOWN_ERROR = "LOCK_UNKNOWN_ERROR";

    private ErrorCodes() {
    }
  }
}
