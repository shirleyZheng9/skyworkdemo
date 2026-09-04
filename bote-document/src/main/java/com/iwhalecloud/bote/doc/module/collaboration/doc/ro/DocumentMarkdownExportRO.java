package com.iwhalecloud.bote.doc.module.collaboration.doc.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档markdown内容导出请求参数
 *
 * @author Aiqing
 * @since 2025/9/24
 */
@Getter
@Setter
@ToString
public class DocumentMarkdownExportRO {

  @Schema(description = "文档ID")
  private String documentId;
}
