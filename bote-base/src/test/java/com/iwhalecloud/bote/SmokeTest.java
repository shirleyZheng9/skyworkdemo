package com.iwhalecloud.bote;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * bote-base 测试基础设施冒烟用例：验证 test-compile、surefire、JaCoCo 报告链路可用。
 */
class SmokeTest {

  @Test
  void smoke() {
    assertThat(true).isTrue();
  }
}
