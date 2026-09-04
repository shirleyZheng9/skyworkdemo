package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.doc.module.knowledge.entity.BtDcQaChunkReferenceEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 问答引用片段表 DTO
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BtDcQaChunkReferenceDTO extends BtDcQaChunkReferenceEntity {
  @Schema(description = "文档名字")
  private String docName;
  @Schema(description = "文档id")
  private Long documentId;
  @Schema(description = "bt_dc_document.document_id文档唯一编码")
  private String dcDocumentId;
  @Schema(description = "文档类型：WORD-Word文档，EXCEL-Excel表格,FOLDER-文件夹等")
  private String dcDocumentType;

  @Schema(description = "知识库名称")
  private String knowledgeName;

  @Schema(description = "所属文档库唯一编码")
  private String libraryId;

  @Schema(description = "Dochain 知识库是否已存在")
  private String isExist;

  @Schema(description = "内容来源：ONLINE-在线文档，UPLOAD-上传文件")
  private String contentSource;
}
