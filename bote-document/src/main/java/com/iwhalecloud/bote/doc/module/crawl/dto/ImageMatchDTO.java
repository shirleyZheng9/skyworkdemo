package com.iwhalecloud.bote.doc.module.crawl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片匹配信息
 *
 * <p>用于在 Markdown 中定位图片语法片段并进行替换。</p>
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
@Getter
@Setter
@ToString
public class ImageMatchDTO {

  @Schema(description = "alt 文本")
  private String altText;

  @Schema(description = "图片 URL")
  private String url;

  @Schema(description = "图片语法在原文中的开始位置（包含 ![ ）")
  private int start;

  @Schema(description = "图片语法在原文中的结束位置（不包含结尾的字符，等于 imageStart..imageEnd 的 end）")
  private int end;
}

