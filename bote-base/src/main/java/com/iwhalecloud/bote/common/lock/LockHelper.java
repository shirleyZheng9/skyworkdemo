package com.iwhalecloud.bote.common.lock;

/**
 * 分布式锁辅助工具类
 *
 * <p>提供锁相关的通用工具方法，如锁键格式化、路径构建等。</p>
 *
 * @author Aiqing
 * @since 2025-01-28
 */
public final class LockHelper {

  private LockHelper() {
    // 工具类，防止实例化
  }

  /**
   * 格式化业务锁键
   *
   * @param bizType 业务类型
   * @param bizId 业务ID
   * @return 格式化后的锁键
   * @throws IllegalArgumentException 如果参数为空
   */
  public static String formatBizLockKey(String bizType, String bizId) {
    if (bizType == null || bizType.trim().isEmpty()) {
      throw new IllegalArgumentException("业务类型不能为空");
    }
    if (bizId == null || bizId.trim().isEmpty()) {
      throw new IllegalArgumentException("业务ID不能为空");
    }

    return String.format("%s:%s", bizType.trim(), bizId.trim());
  }

  /**
   * 格式化租户锁键
   *
   * @param tenantId 租户ID
   * @param lockKey 锁键
   * @return 格式化后的锁键
   * @throws IllegalArgumentException 如果参数为空
   */
  public static String formatTenantLockKey(Long tenantId, String lockKey) {
    if (tenantId == null) {
      throw new IllegalArgumentException("租户ID不能为空");
    }
    if (lockKey == null || lockKey.trim().isEmpty()) {
      throw new IllegalArgumentException("锁键不能为空");
    }

    return String.format("tenant:%d:%s", tenantId, lockKey.trim());
  }

  /**
   * 清理锁键中的非法字符
   *
   * @param lockKey 原始锁键
   * @return 清理后的锁键
   */
  public static String sanitizeLockKey(String lockKey) {
    if (lockKey == null) {
      return null;
    }

    // 移除或替换非法字符，保留字母、数字、下划线、冒号、点、横线
    return lockKey.replaceAll("[^a-zA-Z0-9_:.-]", "_");
  }

  /**
   * 构建锁的完整路径
   *
   * @param basePath 基础路径
   * @param lockKey 锁键
   * @return 完整路径
   */
  public static String buildLockPath(String basePath, String lockKey) {
    if (basePath == null || basePath.trim().isEmpty()) {
      throw new IllegalArgumentException("基础路径不能为空");
    }
    if (lockKey == null || lockKey.trim().isEmpty()) {
      throw new IllegalArgumentException("锁键不能为空");
    }

    String normalizedBasePath = basePath.trim();
    if (!normalizedBasePath.endsWith("/")) {
      normalizedBasePath += "/";
    }

    String safeLockKey = sanitizeLockKey(lockKey.trim());
    return normalizedBasePath + safeLockKey;
  }

  /**
   * 验证锁键的有效性
   *
   * @param lockKey 锁键
   * @throws IllegalArgumentException 如果锁键无效
   */
  public static void validateLockKey(String lockKey) {
    if (lockKey == null || lockKey.trim().isEmpty()) {
      throw new IllegalArgumentException("锁键不能为空");
    }

    String trimmed = lockKey.trim();
    if (trimmed.length() > 250) {
      throw new IllegalArgumentException("锁键长度不能超过250个字符");
    }

    // 检查是否包含特殊的控制字符
    if (trimmed.contains("\n") || trimmed.contains("\r") || trimmed.contains("\t")) {
      throw new IllegalArgumentException("锁键不能包含控制字符");
    }
  }

  /**
   * 生成缓存键前缀
   *
   * @param namespace 命名空间
   * @param prefix 前缀
   * @return 完整的缓存键前缀
   */
  public static String buildCacheKeyPrefix(String namespace, String prefix) {
    StringBuilder sb = new StringBuilder();

    if (namespace != null && !namespace.trim().isEmpty()) {
      sb.append(namespace.trim());
      if (!namespace.endsWith(":")) {
        sb.append(":");
      }
    }

    if (prefix != null && !prefix.trim().isEmpty()) {
      sb.append(prefix.trim());
      if (!prefix.endsWith(":")) {
        sb.append(":");
      }
    }

    return sb.toString();
  }
}
