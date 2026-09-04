package com.iwhalecloud.bote.common.diffc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * {@link CustomizedService} 单元测试。
 *
 * <p>getUserId 依赖 SessionUtil.getOptionalUserId，以 mockStatic 注入；状态码取自常量。</p>
 */
class CustomizedServiceTest {

  @Test
  void getUserId_delegatesToSessionUtil() {
    CustomizedService service = new CustomizedService();
    try (MockedStatic<SessionUtil> sessionUtil = mockStatic(SessionUtil.class)) {
      sessionUtil.when(() -> SessionUtil.getOptionalUserId(-1L)).thenReturn(42L);
      assertThat(service.getUserId()).isEqualTo(42L);
    }
  }

  @Test
  void getUserId_noSession_returnsDefault() {
    CustomizedService service = new CustomizedService();
    try (MockedStatic<SessionUtil> sessionUtil = mockStatic(SessionUtil.class)) {
      sessionUtil.when(() -> SessionUtil.getOptionalUserId(-1L)).thenReturn(-1L);
      assertThat(service.getUserId()).isEqualTo(-1L);
    }
  }

  @Test
  void getValidStatusCd_returnsConstant() {
    assertThat(new CustomizedService().getValidStatusCd()).isEqualTo(CommonConsts.STATUS_CD_VALID);
  }

  @Test
  void getInvalidStatusCd_returnsConstant() {
    assertThat(new CustomizedService().getInvalidStatusCd()).isEqualTo(CommonConsts.STATUS_CD_INVALID);
  }
}
