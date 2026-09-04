package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 租户 ID 工具类
 *
 * @author bianjp
 * @since 2024-10-08
 */
public final class TenantIdUtil {
  /** 租户 ID 请求头名称 */
  private static final String TENANT_ID_HEADER = "Tenant-Id";
  /** 租户 ID URL 参数名称 */
  private static final String TENANT_ID_PARAM = "tenantId";
  /** 租户 ID 线程变量 */
  private static final ThreadLocal<Long> tenantIdThreadLocal = new ThreadLocal<>();
  private static final TenantQueryMapper tenantQueryMapper = SpringUtil.getBean(TenantQueryMapper.class);
  private static final WorkspaceManageMapper spaceQueryMapper = SpringUtil.getBean(WorkspaceManageMapper.class);

  private TenantIdUtil() {
  }

  /**
   * 获取租户 ID, 不存在时报错
   */
  public static Long getTenantId() {
    Long tenantId = tenantIdThreadLocal.get();
    if (tenantId == null) {
      throw new BssException("无法获取 tenantId");
    }
    return tenantId;
  }

  /**
   * 获取租户 ID, 不存在时返回 null
   */
  @Nullable
  public static Long getTenantIdOptional() {
    return tenantIdThreadLocal.get();
  }

  /**
   * 从 HTTP 请求中获取租户 ID
   */
  @Nullable
  public static Long getTenantIdFromRequest(HttpServletRequest request) {
    // 优先取 URL 参数
    String tenantId = request.getParameter(TENANT_ID_PARAM);
    if (StringUtils.isNumeric(tenantId)) {
      return Long.parseLong(tenantId);
    }
    // 其次取请求头
    tenantId = request.getHeader(TENANT_ID_HEADER);
    if (StringUtils.isNumeric(tenantId)) {
      return Long.parseLong(tenantId);
    }
    return null;
  }

  /**
   * 设置租户 ID 到线程本地变量
   */
  public static void setTenantId(@Nullable Long tenantId) {
    if (tenantId == null) {
      clearThreadLocal();
    }
    else {
      tenantIdThreadLocal.set(tenantId);
    }
  }

  /**
   * 根据 spaceId 调整 tenantId
   *
   * <p>仅供文档中心使用，AI 门户场景，需要根据空间 ID 计算实际的 tenantId </p>
   */
  @Nullable
  public static Long getSpaceTenantId(Long spaceId) {
    return spaceQueryMapper.getVirtualTenantId(spaceId);
  }

  /**
   * 根据租户 ID 查询归属的空间 ID
   */
  public static Long getSpaceId(Long tenantId) {
    return tenantQueryMapper.getSpaceId(tenantId);
  }

  /**
   * 清理线程本地变量
   */
  public static void clearThreadLocal() {
    tenantIdThreadLocal.remove();
  }
}
