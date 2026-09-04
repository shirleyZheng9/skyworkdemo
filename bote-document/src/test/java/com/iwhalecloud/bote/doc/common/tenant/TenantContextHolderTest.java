package com.iwhalecloud.bote.doc.common.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * {@link TenantContextHolder} 单元测试。
 *
 * <p>ThreadLocal 隔离的租户上下文，@AfterEach 清理避免线程内泄漏。覆盖 tenantId/ignore 的
 * 读写往返、getRequiredTenantId 的空值抛 NPE、setIgnore(null) 等价非忽略、clear 清理。</p>
 */
class TenantContextHolderTest {

  @AfterEach
  void clear() {
    TenantContextHolder.clear();
  }

  @Test
  void getTenantId_defaultsNull_andRoundTrips() {
    assertThat(TenantContextHolder.getTenantId()).isNull();
    TenantContextHolder.setTenantId(7L);
    assertThat(TenantContextHolder.getTenantId()).isEqualTo(7L);
  }

  @Test
  void getRequiredTenantId_present_returnsValue() {
    TenantContextHolder.setTenantId(99L);
    assertThat(TenantContextHolder.getRequiredTenantId()).isEqualTo(99L);
  }

  @Test
  void getRequiredTenantId_absent_throwsNpe() {
    assertThatThrownBy(TenantContextHolder::getRequiredTenantId)
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("租户编号");
  }

  @Test
  void isIgnore_defaultsFalse_andRoundTrips() {
    assertThat(TenantContextHolder.isIgnore()).isFalse();
    TenantContextHolder.setIgnore(true);
    assertThat(TenantContextHolder.isIgnore()).isTrue();
    TenantContextHolder.setIgnore(false);
    assertThat(TenantContextHolder.isIgnore()).isFalse();
  }

  @Test
  void setIgnore_null_treatedAsNotIgnore() {
    TenantContextHolder.setIgnore(null);
    assertThat(TenantContextHolder.isIgnore()).isFalse();
  }

  @Test
  void clear_removesTenantIdAndIgnore() {
    TenantContextHolder.setTenantId(1L);
    TenantContextHolder.setIgnore(true);
    TenantContextHolder.clear();
    assertThat(TenantContextHolder.getTenantId()).isNull();
    assertThat(TenantContextHolder.isIgnore()).isFalse();
  }
}
