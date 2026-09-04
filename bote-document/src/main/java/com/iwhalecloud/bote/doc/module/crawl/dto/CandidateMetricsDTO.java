package com.iwhalecloud.bote.doc.module.crawl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 正文内“非正文块”识别指标（供清洗逻辑使用）
 *
 * <p>从 {@code CleanHtmlStep} 内部类抽取后，调整为 DTO。</p>
 */
@Getter
@Setter
@ToString
public class CandidateMetricsDTO {

  @Schema(description = "块内段落数量（p 标签）")
  private int pCount;

  @Schema(description = "块内文本长度（trim 后）")
  private int textLen;

  @Schema(description = "块内链接数量（a[href]）")
  private int linkCount;

  @Schema(description = "块内列表项数量（li 标签）")
  private int liCount;

  @Schema(description = "块内图片数量（img 标签）")
  private int imgCount;

  @Schema(description = "链接文本密度（链接文本长度 / 总文本长度）")
  private double linkDensity;

  @Schema(description = "非链接文本长度（总文本长度 - 链接文本长度）")
  private int nonLinkTextLen;

  @Schema(description = "链接文本为空但包含媒体/子元素的链接数量（用于识别卡片流）")
  private int anchorWithNoTextCount;
}

