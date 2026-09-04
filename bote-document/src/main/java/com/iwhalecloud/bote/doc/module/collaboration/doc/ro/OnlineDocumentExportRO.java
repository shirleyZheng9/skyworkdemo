package com.iwhalecloud.bote.doc.module.collaboration.doc.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线文档导出请求参数
 *
 * @author Aiqing
 * @since 2025/9/25
 */
@Getter
@Setter
@ToString
public class OnlineDocumentExportRO {

  @Schema(description = "文档ID")
  private String documentId;
}
