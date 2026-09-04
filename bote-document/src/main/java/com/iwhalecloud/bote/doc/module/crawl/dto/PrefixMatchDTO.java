package com.iwhalecloud.bote.doc.module.crawl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * URL 前缀匹配结果 DTO（从 {@code PrefixMatch} 调整而来）
 */
@Getter
@Setter
@ToString
public class PrefixMatchDTO {

  @Schema(description = "替换后的 URL")
  private String replacement;

  @Schema(description = "匹配长度（用于选择最长前缀匹配）")
  private int matchLength;
}

