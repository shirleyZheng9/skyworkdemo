package com.iwhalecloud.bote.doc.module.dtable.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Aiqing
 * @since 2026/1/4
 */
@Getter
@Setter
@ToString
@Schema(description = "文档关联多维表格信息")
public class DocumentDimTableRelaDTO {

  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "关联空间ID")
  private String spaceId;
  @Schema(description = "默认打开的节点ID")
  private String nodeId;
}
