package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * {@link TenantIdUtil} 单元测试
 *
 * <p>TenantIdUtil 的静态字段在类加载时通过 {@code SpringUtil.getBean} 获取 Mapper。
 * 使用 mockStatic(SpringUtil) 在类首次加载前注入 mock Mapper，使类加载成功。
 * 之后覆盖 getTenantId/getTenantIdOptional/setTenantId/clearThreadLocal（ThreadLocal）、
 * getTenantIdFromRequest（URL 参数 > 请求头 > null）、getSpaceTenantId/getSpaceId（Mapper 调用）。</p>
 */
class TenantIdUtilTest {

  private static MockedStatic<SpringUtil> springMock;
  private static TenantQueryMapper tenantQueryMapperMock;
  private static WorkspaceManageMapper workspaceManageMapperMock;

  @BeforeAll
  static void setUp() {
    tenantQueryMapperMock = mock(TenantQueryMapper.class);
    workspaceManageMapperMock = mock(WorkspaceManageMapper.class);
    springMock = mockStatic(SpringUtil.class);
    springMock.when(() -> SpringUtil.getBean(TenantQueryMapper.class)).thenReturn(tenantQueryMapperMock);
    springMock.when(() -> SpringUtil.getBean(WorkspaceManageMapper.class)).thenReturn(workspaceManageMapperMock);
  }

  @AfterAll
  static void tearDown() {
    springMock.close();
  }

  @AfterEach
  void cleanThreadLocal() {
    TenantIdUtil.clearThreadLocal();
  }

  // ==================== ThreadLocal ====================

  @Test
  void getTenantId_notSet_throwsBssException() {
    assertThatThrownBy(TenantIdUtil::getTenantId)
        .isInstanceOf(BssException.class)
        .hasMessageContaining("tenantId");
  }

  @Test
  void getTenantId_set_returnsValue() {
    TenantIdUtil.setTenantId(42L);
    assertThat(TenantIdUtil.getTenantId()).isEqualTo(42L);
  }

  @Test
  void getTenantIdOptional_notSet_returnsNull() {
    assertThat(TenantIdUtil.getTenantIdOptional()).isNull();
  }

  @Test
  void getTenantIdOptional_set_returnsValue() {
    TenantIdUtil.setTenantId(99L);
    assertThat(TenantIdUtil.getTenantIdOptional()).isEqualTo(99L);
  }

  @Test
  void setTenantId_null_clearsThreadLocal() {
    TenantIdUtil.setTenantId(1L);
    TenantIdUtil.setTenantId(null);
    assertThat(TenantIdUtil.getTenantIdOptional()).isNull();
  }

  @Test
  void clearThreadLocal_removesValue() {
    TenantIdUtil.setTenantId(5L);
    TenantIdUtil.clearThreadLocal();
    assertThat(TenantIdUtil.getTenantIdOptional()).isNull();
  }

  // ==================== getTenantIdFromRequest ====================

  @Test
  void getTenantIdFromRequest_urlParam_returnsValue() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("tenantId")).thenReturn("123");

    assertThat(TenantIdUtil.getTenantIdFromRequest(request)).isEqualTo(123L);
  }

  @Test
  void getTenantIdFromRequest_headerParam_returnsValue() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("tenantId")).thenReturn(null);
    when(request.getHeader("Tenant-Id")).thenReturn("456");

    assertThat(TenantIdUtil.getTenantIdFromRequest(request)).isEqualTo(456L);
  }

  @Test
  void getTenantIdFromRequest_urlParamTakesPrecedenceOverHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("tenantId")).thenReturn("111");
    when(request.getHeader("Tenant-Id")).thenReturn("222");

    assertThat(TenantIdUtil.getTenantIdFromRequest(request)).isEqualTo(111L);
  }

  @Test
  void getTenantIdFromRequest_nonNumericParam_returnsNullThenChecksHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("tenantId")).thenReturn("abc");
    when(request.getHeader("Tenant-Id")).thenReturn("789");

    assertThat(TenantIdUtil.getTenantIdFromRequest(request)).isEqualTo(789L);
  }

  @Test
  void getTenantIdFromRequest_noValidParamOrHeader_returnsNull() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("tenantId")).thenReturn(null);
    when(request.getHeader("Tenant-Id")).thenReturn(null);

    assertThat(TenantIdUtil.getTenantIdFromRequest(request)).isNull();
  }

  @Test
  void getTenantIdFromRequest_nonNumericBoth_returnsNull() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("tenantId")).thenReturn("notnum");
    when(request.getHeader("Tenant-Id")).thenReturn("alsonotnum");

    assertThat(TenantIdUtil.getTenantIdFromRequest(request)).isNull();
  }

  // ==================== Mapper 方法 ====================

  @Test
  void getSpaceTenantId_delegatesToMapper() {
    when(workspaceManageMapperMock.getVirtualTenantId(10L)).thenReturn(20L);
    assertThat(TenantIdUtil.getSpaceTenantId(10L)).isEqualTo(20L);
  }

  @Test
  void getSpaceId_delegatesToMapper() {
    when(tenantQueryMapperMock.getSpaceId(30L)).thenReturn(40L);
    assertThat(TenantIdUtil.getSpaceId(30L)).isEqualTo(40L);
  }
}
