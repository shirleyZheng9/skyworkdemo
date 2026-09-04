package com.iwhalecloud.bote.dto.beyond;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应目录项
 *
 * @author lizuyin
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class BeyondCatalogItem {
  /** 目录ID */
  private Integer catalogId;
  /** 目录名称 */
  private String catalogName;
  /** 目录描述 */
  private String catalogDesc;
  /** 父目录ID */
  @JsonProperty("pCatalogId")
  private Integer pCatalogId;
}
