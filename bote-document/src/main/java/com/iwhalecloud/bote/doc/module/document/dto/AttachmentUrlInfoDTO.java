package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "文档")
public class AttachmentUrlInfoDTO {
  @Schema(description = "文档库文档id")
  private String documentId;
  @Schema(description = "文档库文档名称")
  private String fileName;

  public AttachmentUrlInfoDTO(String documentId, String fileName) {
    this.documentId = documentId;
    this.fileName = fileName;
  }
}
