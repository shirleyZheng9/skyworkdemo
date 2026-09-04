package com.iwhalecloud.bote.common.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * {@link BaseSystemParameter} 单元测试
 *
 * <p>覆盖 getValueFromEnv（SpringUtil.getProperty）、getValueFromDb（DcParamCache）、
 * getIntegerValueFromDb/getRequiredIntegerValueFromDb 的空值与非空分支、
 * getBooleanValueFromDb 的 T/Y/TRUE/1/F/空/非法各分支。mockStatic(SpringUtil) 贯穿每个用例。</p>
 */
class BaseSystemParameterTest {

  private MockedStatic<SpringUtil> spring;
  private DcParamCache dcParamCache;

  @BeforeEach
  void setUp() {
    spring = mockStatic(SpringUtil.class);
    dcParamCache = mock(DcParamCache.class);
    spring.when(() -> SpringUtil.getBean(DcParamCache.class)).thenReturn(dcParamCache);
  }

  @AfterEach
  void tearDown() {
    spring.close();
  }

  @Test
  void getValueFromEnv_returnsPropertyFromSpring() {
    spring.when(() -> SpringUtil.getProperty(anyString(), anyString())).thenReturn("env-val");
    assertThat(BaseSystemParameter.FLOW_EXECUTION_TIME_LIMIT.getValueFromEnv()).isEqualTo("env-val");
  }

  @Test
  void getValueFromDb_returnsValueFromCache() {
    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("db-val");
    assertThat(BaseSystemParameter.FLOW_EXECUTION_TIME_LIMIT.getValueFromDb()).isEqualTo("db-val");
  }

  @Test
  void getIntegerValueFromDb_validValue_returnsInteger() {
    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("300");
    assertThat(BaseSystemParameter.FLOW_EXECUTION_TIME_LIMIT.getIntegerValueFromDb()).isEqualTo(300);
  }

  @Test
  void getIntegerValueFromDb_emptyValue_returnsNull() {
    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("");
    assertThat(BaseSystemParameter.FLOW_EXECUTION_TIME_LIMIT.getIntegerValueFromDb()).isNull();
  }

  @Test
  void getRequiredIntegerValueFromDb_validValue_returnsInteger() {
    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("300");
    assertThat(BaseSystemParameter.FLOW_EXECUTION_TIME_LIMIT.getRequiredIntegerValueFromDb()).isEqualTo(300);
  }

  @Test
  void getRequiredIntegerValueFromDb_emptyValue_returnsZero() {
    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("");
    assertThat(BaseSystemParameter.FLOW_EXECUTION_TIME_LIMIT.getRequiredIntegerValueFromDb()).isZero();
  }

  @Test
  void getBooleanValueFromDb_trueFlags_returnTrue() {
    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("T");
    assertThat(BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()).isTrue();

    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("1");
    assertThat(BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()).isTrue();

    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("true");
    assertThat(BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()).isTrue();

    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("Y");
    assertThat(BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()).isTrue();
  }

  @Test
  void getBooleanValueFromDb_falseFlags_returnFalse() {
    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("F");
    assertThat(BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()).isFalse();

    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("");
    assertThat(BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()).isFalse();

    when(dcParamCache.getDcParamValByCode(anyString(), anyString())).thenReturn("garbage");
    assertThat(BaseSystemParameter.DOCUMENT_HIS_LOG_ENABLED.getBooleanValueFromDb()).isFalse();
  }

  @Test
  void getters_returnCodeAndDefault() {
    assertThat(BaseSystemParameter.FLOW_EXECUTION_TIME_LIMIT.getCode()).isEqualTo("FLOW_EXECUTION_TIME_LIMIT");
    assertThat(BaseSystemParameter.FLOW_EXECUTION_TIME_LIMIT.getDefaultValue()).isEqualTo("300");
  }
}
