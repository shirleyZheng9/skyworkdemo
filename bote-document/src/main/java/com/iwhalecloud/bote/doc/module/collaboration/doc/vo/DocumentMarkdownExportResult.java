package com.iwhalecloud.bote.doc.module.collaboration.doc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线文档markdown内容导出结果
 *
 * @author Aiqing
 * @since 2025/9/24
 */
@Getter
@Setter
@ToString
public class DocumentMarkdownExportResult {

  @Schema(description = "markdown格式内容")
  private String content;
}
