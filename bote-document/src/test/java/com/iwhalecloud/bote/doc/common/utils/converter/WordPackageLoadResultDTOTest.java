package com.iwhalecloud.bote.doc.common.utils.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/**
 * {@link WordPackageLoadResultDTO} 单元测试。
 *
 * <p>薄 DTO：Lombok 生成的 getter/setter/toString 已被 @Generated 排除，
 * 此处覆盖手写构造器与 cleanup 回调执行。不启动 Spring 容器、不联网、不连 DB。</p>
 */
class WordPackageLoadResultDTOTest {

  @Test
  void constructor_andGetters_returnValues() {
    AtomicBoolean called = new AtomicBoolean(false);
    Runnable cleanup = () -> called.set(true);
    WordPackageLoadResultDTO dto = new WordPackageLoadResultDTO(null, cleanup);

    assertThat(dto.getPkg()).isNull();
    assertThat(dto.getCleanup()).isSameAs(cleanup);

    // 执行 cleanup 回调验证可调用
    dto.getCleanup().run();
    assertThat(called.get()).isTrue();
  }
}
