package com.iwhalecloud.bote.common.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import lombok.Getter;

/**
 * 知识库目录枚举
 *
 * @author lxs
 * @since 2024/12/16
 */
@Getter
public enum KnowledgeCatalogEnum {

  /** 我创建的 */
  MY_FOLDER(-1L, "我创建的"),
  /** 他人分享的 */
  OTHER_SHARE(-2L, "他人分享的");

  private final Long catalogId;

  private final String catalogName;

  KnowledgeCatalogEnum(final Long catalogId, final String catalogName) {
    this.catalogId = catalogId;
    this.catalogName = catalogName;
  }

  public static List<KnowledgeCatalogEnum> getEnums() {
    return Arrays.asList(values());
  }

  public static List<Long> getCatalogs() {
    List<Long> catalogs = new ArrayList<>();
    for (KnowledgeCatalogEnum value : values()) {
      catalogs.add(value.getCatalogId());
    }
    return catalogs;
  }
}
