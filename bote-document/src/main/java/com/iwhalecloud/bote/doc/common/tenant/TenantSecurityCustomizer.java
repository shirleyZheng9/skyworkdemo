package com.iwhalecloud.bote.doc.common.tenant;

/**
 * 租户安全扩展自定义处理
 *
 * @author Aiqing
 * @since 2025/9/6
 */
public interface TenantSecurityCustomizer {

  /**
   * 检查访问权限
   *
   * @param tenantId 租户ID
   * @return 是否能够访问
   */
  boolean checkAccess(Long tenantId);
}
