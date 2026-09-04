package com.iwhalecloud.bote.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * {@link OperClassEnum} 单元测试
 *
 * <p>纯常量枚举（无查找方法），覆盖 values/valueOf 契约与常量存在性。</p>
 */
class OperClassEnumTest {

  @Test
  void values_containsExpectedConstants() {
    assertThat(OperClassEnum.values())
        .contains(
            OperClassEnum.BOT,
            OperClassEnum.SCENE,
            OperClassEnum.KNOWLEDGE,
            OperClassEnum.JOB,
            OperClassEnum.DO_NOT_USE);
  }

  @Test
  void valueOf_roundtripsForEachConstant() {
    for (OperClassEnum e : OperClassEnum.values()) {
      assertThat(OperClassEnum.valueOf(e.name())).isSameAs(e);
    }
  }
}
