package com.iwhalecloud.bote.dto.knowledge.docchain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DocChain 文档信息，包含文档内容
 *
 * @author bianjp
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
@Schema(description = "DocChain 文档信息，包含文档内容")
public class DocChainDocumentWithContentDTO {
  @Schema(description = "文档 ID")
  private Long docId;
  @Schema(description = "文档名称")
  private String docName;
  @Schema(description = "文档内容(markdown 格式)")
  private String content;
}
