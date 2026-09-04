package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档发布状态
 */
@Getter
@Setter
@ToString
public class DocumentReleasedDTO {

  @Schema(description = "文档唯一编码")
  private String documentId;
  @Schema(description = "文档名称")
  private String documentName;
  @Schema(description = "文档类型：WORD-Word文档，EXCEL-Excel表格,FOLDER-文件夹等")
  private String documentType;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "空间ID")
  private Long spaceId;
  @Schema(description = "是否需要发布：T为需要发布更新应用过的知识库文档")
  private String released;
}
