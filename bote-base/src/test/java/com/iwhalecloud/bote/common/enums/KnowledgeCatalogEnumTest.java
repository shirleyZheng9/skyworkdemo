package com.iwhalecloud.bote.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link KnowledgeCatalogEnum} 单元测试
 *
 * <p>覆盖 getEnums/getCatalogs 与构造的 catalogId/catalogName。</p>
 */
class KnowledgeCatalogEnumTest {

  @Test
  void getEnums_returnsAllConstants() {
    assertThat(KnowledgeCatalogEnum.getEnums())
        .containsExactly(KnowledgeCatalogEnum.MY_FOLDER, KnowledgeCatalogEnum.OTHER_SHARE);
  }

  @Test
  void getCatalogs_returnsAllIds() {
    List<Long> catalogs = KnowledgeCatalogEnum.getCatalogs();
    assertThat(catalogs).containsExactly(-1L, -2L);
  }

  @Test
  void getters_returnExpectedValues() {
    assertThat(KnowledgeCatalogEnum.MY_FOLDER.getCatalogId()).isEqualTo(-1L);
    assertThat(KnowledgeCatalogEnum.MY_FOLDER.getCatalogName()).isEqualTo("我创建的");
    assertThat(KnowledgeCatalogEnum.OTHER_SHARE.getCatalogId()).isEqualTo(-2L);
    assertThat(KnowledgeCatalogEnum.OTHER_SHARE.getCatalogName()).isEqualTo("他人分享的");
  }
}
