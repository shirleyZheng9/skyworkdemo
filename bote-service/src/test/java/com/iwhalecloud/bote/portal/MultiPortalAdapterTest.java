package com.iwhalecloud.bote.portal;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 多门户默认租户兜底策略测试。
 */
class MultiPortalAdapterTest {

  @Test
  void tgPortalSkipsDefaultTenantFallback() {
    LoginInfo loginInfo = LoginInfo.builder()
      .defaultTenantId(BaseConsts.DEFAULT_TENANT_ID)
      .attributes(Map.of(BaseConsts.ATTR_SKIP_DEFAULT_TENANT_FALLBACK, Boolean.TRUE))
      .build();

    assertNull(MultiPortalAdapter.getDefaultTenantFallback(loginInfo));
  }

  @Test
  void otherPortalsKeepExistingDefaultTenantFallback() {
    LoginInfo loginInfo = LoginInfo.builder()
      .defaultTenantId(BaseConsts.DEFAULT_TENANT_ID)
      .attributes(Collections.emptyMap())
      .build();

    assertEquals(BaseConsts.DEFAULT_TENANT_ID,
      MultiPortalAdapter.getDefaultTenantFallback(loginInfo));
  }
}
