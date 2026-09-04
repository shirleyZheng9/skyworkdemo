package com.iwhalecloud.bote.doc.module.collaboration.doc.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 导入在线文档内容请求参数
 *
 * @author lizuyin
 * @since 2025/10/22
 */
@Getter
@Setter
@ToString
public class DocumentImportRO {

  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "导入内容格式", allowableValues = {"html", "markdown"})
  private String format;
  @Schema(description = "导入内容")
  private String content;
  @Schema(description = "操作用户ID")
  private String userId;
}

