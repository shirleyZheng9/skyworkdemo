package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档库文档简单信息
 *
 */
@Getter
@Setter
@ToString
public class SimpleLibDocumentDTO {
  @Schema(description = "文档 ID")
  private Long documentId;
  @Schema(description = "文档库文档id")
  private String dcDocumentId;
  @Schema(description = "所属文档库唯一编码")
  private String libraryId;
  @Schema(description = "外系统 ID")
  private String extSystemId;
  @Schema(description = "知识库id")
  private Long knowledgeId;
}
