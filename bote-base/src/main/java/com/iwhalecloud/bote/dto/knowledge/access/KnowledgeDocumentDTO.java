package com.iwhalecloud.bote.dto.knowledge.access;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档信息，包含文档内容
 *
 * @author lxs
 * @since 2025/07/28
 */
@Getter
@Setter
@ToString
@Schema(description = "文档信息，包含文档内容")
public class KnowledgeDocumentDTO {
  @Schema(description = "文档 ID")
  private Long docId;
  @Schema(description = "文档名称")
  private String docName;
  @Schema(description = "文档内容(markdown 格式)")
  private String content;
}
