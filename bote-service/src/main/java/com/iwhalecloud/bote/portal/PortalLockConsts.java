package com.iwhalecloud.bote.portal;

/**
 * 门户相关分布式锁业务类型常量。
 *
 * @author qian.sisheng
 * @since 2026-04-21
 */
public final class PortalLockConsts {

  /**
   * 门户用户同步锁（同一 systemCode + 外部用户标识互斥）
   */
  public static final String PORTAL_USER_SYNC_LOCK = "portal_user_sync_lock";

  /**
   * 门户租户同步锁(同一 systemCode + 外部租户/项目标识 extTenantId 互斥),防止并发建重租户
   */
  public static final String PORTAL_TENANT_SYNC_LOCK = "portal_tenant_sync_lock";

  private PortalLockConsts() {
    throw new IllegalStateException();
  }
}
